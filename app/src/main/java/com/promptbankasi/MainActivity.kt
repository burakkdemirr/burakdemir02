package com.promptbankasi

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.promptbankasi.data.PromptRepository
import com.promptbankasi.notifications.NotificationScheduler
import com.promptbankasi.ui.PromptViewModel
import com.promptbankasi.ui.theme.PromptBankasiTheme

class MainActivity : ComponentActivity() {

    private val vm: PromptViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationScheduler.scheduleDailyPromptNotification(this)

        setContent {
            val state by vm.uiState.collectAsState()
            PromptBankasiTheme(darkTheme = state.isDarkTheme) {
                PromptBankasiApp(vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptBankasiApp(viewModel: PromptViewModel = viewModel()) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Prompt Bankası") },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                        actions = {
                            IconButton(onClick = { viewModel.toggleTheme() }) {
                                Icon(
                                    imageVector = if (state.isDarkTheme) Icons.Default.WbSunny else Icons.Default.DarkMode,
                                    contentDescription = "Tema"
                                )
                            }
                            IconButton(onClick = { navController.navigate("favorites") }) {
                                Icon(Icons.Default.Favorite, contentDescription = "Favoriler")
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { navController.navigate("create") }) {
                        Icon(Icons.Default.Add, contentDescription = "Prompt Ekle")
                    }
                }
            ) { padding ->
                HomeScreen(
                    modifier = Modifier.padding(padding),
                    viewModel = viewModel,
                    onPromptClick = { navController.navigate("detail/${it}") },
                    onNotifyClick = {
                        NotificationScheduler.showInstantNotification(context)
                        Toast.makeText(context, "Bugünün prompt bildirimi gönderildi", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        composable(
            route = "detail/{promptId}",
            arguments = listOf(navArgument("promptId") { type = NavType.StringType })
        ) { backStack ->
            val promptId = backStack.arguments?.getString("promptId")
            val prompt = state.prompts.find { it.id == promptId }

            if (prompt != null) {
                PromptDetailScreen(
                    prompt = prompt,
                    isFavorite = prompt.id in state.favorites,
                    onBack = { navController.popBackStack() },
                    onCopy = {
                        copyToClipboard(context, prompt.promptText)
                        viewModel.incrementUsage(prompt.id)
                    },
                    onFavorite = { viewModel.toggleFavorite(prompt.id) },
                    onShare = { shareText(context, prompt.promptText) }
                )
            }
        }

        composable("favorites") {
            FavoritesScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onPromptClick = { navController.navigate("detail/$it") }
            )
        }

        composable("create") {
            CreatePromptScreen(
                onBack = { navController.popBackStack() },
                onSave = { categoryId, title, description, promptText ->
                    viewModel.addCustomPrompt(categoryId, title, description, promptText)
                    navController.popBackStack()
                },
                onGenerateWithAi = { topic, goal ->
                    viewModel.generatePromptWithAi(topic, goal)
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: PromptViewModel,
    onPromptClick: (String) -> Unit,
    onNotifyClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val popular = state.prompts.filter { it.isPopular }.sortedByDescending { it.usageCount }
    val topUsed = state.prompts.sortedByDescending { it.usageCount }.take(5)
    val dayPrompt = topUsed.firstOrNull()

    val filtered = state.prompts.filter { prompt ->
        val query = state.searchQuery.trim().lowercase()
        if (query.isBlank()) true else {
            prompt.title.lowercase().contains(query) ||
                prompt.description.lowercase().contains(query) ||
                prompt.promptText.lowercase().contains(query)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::setSearch,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Prompt ara") },
                placeholder = { Text("Örn: CV, Instagram, Kotlin...") }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥 Günün Promptu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                AssistChip(onClick = onNotifyClick, label = { Text("Bildirim Gönder") }, leadingIcon = {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                })
            }
            if (dayPrompt != null) {
                PromptCard(dayPrompt, onClick = { onPromptClick(dayPrompt.id) })
            }
        }

        item {
            Text("Kategoriler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(PromptRepository.categories) { category ->
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text("${category.emoji} ${category.name}")
                    }
                }
            }
        }

        item {
            Text("⭐ En Popüler Promptlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(popular) { prompt ->
            PromptCard(prompt, onClick = { onPromptClick(prompt.id) })
        }

        item {
            Text("📈 En Çok Kullanılanlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(topUsed) { prompt ->
            PromptCard(prompt, onClick = { onPromptClick(prompt.id) })
        }

        item {
            Text("Tüm Promptlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(filtered) { prompt ->
            PromptCard(prompt, onClick = { onPromptClick(prompt.id) })
        }

        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("İletişim", fontWeight = FontWeight.Bold)
                    Text("Burak Demir")
                    Text("burakk.demirr.02@gmail.com")
                    Text("Offline destek: Promptlar cihazda saklanır ve internetsiz görüntülenebilir.")
                }
            }
        }
    }
}

@Composable
fun PromptCard(prompt: com.promptbankasi.data.PromptItem, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(prompt.title, fontWeight = FontWeight.Bold)
            Text(prompt.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("Kullanım: ${prompt.usageCount}", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptDetailScreen(
    prompt: com.promptbankasi.data.PromptItem,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onCopy: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Prompt Detayı") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(prompt.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(prompt.description)
            Card {
                Text(prompt.promptText, modifier = Modifier.padding(16.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(Modifier.padding(3.dp))
                    Text("Kopyala")
                }
                Button(onClick = onFavorite) {
                    Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null)
                    Spacer(Modifier.padding(3.dp))
                    Text("Favorilere Ekle")
                }
                Button(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.padding(3.dp))
                    Text("Paylaş")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(state: com.promptbankasi.ui.PromptUiState, onBack: () -> Unit, onPromptClick: (String) -> Unit) {
    val favorites = state.prompts.filter { it.id in state.favorites }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Favoriler") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (favorites.isEmpty()) {
                item { Text("Henüz favorilere eklenmiş prompt yok.") }
            } else {
                items(favorites) { prompt -> PromptCard(prompt) { onPromptClick(prompt.id) } }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePromptScreen(
    onBack: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
    onGenerateWithAi: (String, String) -> String
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var promptText by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(PromptRepository.categories.first().id) }
    var aiTopic by rememberSaveable { mutableStateOf("") }
    var aiGoal by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Prompt Oluştur") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("AI ile otomatik prompt oluştur", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = aiTopic, onValueChange = { aiTopic = it }, label = { Text("Konu") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = aiGoal, onValueChange = { aiGoal = it }, label = { Text("Hedef") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { promptText = onGenerateWithAi(aiTopic, aiGoal) }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(Modifier.padding(2.dp))
                    Text("AI ile Üret")
                }
            }
            item {
                Text("Kendi promptunu ekle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Başlık") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Açıklama") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = promptText, onValueChange = { promptText = it }, label = { Text("Prompt Metni") }, modifier = Modifier.fillMaxWidth())

                Text("Kategori Seç")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PromptRepository.categories) { cat ->
                        AssistChip(
                            onClick = { selectedCategory = cat.id },
                            label = { Text("${cat.emoji} ${cat.name}") }
                        )
                    }
                }

                Button(
                    onClick = { onSave(selectedCategory, title, description, promptText) },
                    enabled = title.isNotBlank() && promptText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kaydet")
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Prompt", text))
    Toast.makeText(context, "Prompt kopyalandı", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Promptu paylaş"))
}
