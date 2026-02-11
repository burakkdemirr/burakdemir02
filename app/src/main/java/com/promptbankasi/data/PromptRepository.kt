package com.promptbankasi.data

object PromptRepository {

    val categories = listOf(
        PromptCategory("egitim", "📚", "Eğitim"),
        PromptCategory("is", "💼", "İş & Kariyer"),
        PromptCategory("sosyal", "📱", "Sosyal Medya"),
        PromptCategory("icerik", "✍️", "İçerik Üretimi"),
        PromptCategory("kodlama", "💻", "Kodlama"),
        PromptCategory("pazarlama", "🎯", "Pazarlama"),
        PromptCategory("tasarim", "🎨", "Tasarım"),
        PromptCategory("kisisel", "🧠", "Kişisel Gelişim"),
        PromptCategory("resmi", "🧑‍⚖️", "Resmi Yazışma"),
        PromptCategory("eglence", "🧩", "Eğlenceli Promptlar"),
        PromptCategory("araba", "🚗", "Araba")
    )

    val starterPrompts = listOf(
        PromptItem(
            id = "p1",
            categoryId = "egitim",
            title = "Konu Anlatımı Sadeleştirici",
            description = "Zor bir konuyu öğrencinin seviyesine göre açıklar.",
            promptText = "Aşağıdaki konuyu 10. sınıf öğrencisi için sade, örnekli ve adım adım anlat: [KONU]",
            usageCount = 214,
            isPopular = true
        ),
        PromptItem(
            id = "p2",
            categoryId = "is",
            title = "CV Güçlendirme Danışmanı",
            description = "CV metnini daha profesyonel hale getirir.",
            promptText = "Bu CV özetini ATS uyumlu ve profesyonel bir dille yeniden yaz: [CV METNİ]",
            usageCount = 180,
            isPopular = true
        ),
        PromptItem(
            id = "p3",
            categoryId = "sosyal",
            title = "Instagram İçerik Planı",
            description = "7 günlük sosyal medya içerik takvimi üretir.",
            promptText = "[SEKTÖR] için hedef kitleye uygun 7 günlük Instagram içerik planı oluştur.",
            usageCount = 162,
            isPopular = true
        ),
        PromptItem(
            id = "p4",
            categoryId = "kodlama",
            title = "Kod İnceleme Asistanı",
            description = "Kod kalitesi ve performans önerileri verir.",
            promptText = "Aşağıdaki Kotlin kodunu okunabilirlik, performans ve best practice açısından incele: [KOD]",
            usageCount = 145
        ),
        PromptItem(
            id = "p5",
            categoryId = "resmi",
            title = "Resmi E-posta Yazımı",
            description = "Kurumsal ve nazik e-posta taslağı hazırlar.",
            promptText = "[KONU] hakkında resmi, kısa ve net bir e-posta taslağı yaz. Hitap ve kapanış ekle.",
            usageCount = 134
        ),
        PromptItem(
            id = "p6",
            categoryId = "araba",
            title = "Araç Karşılaştırma Uzmanı",
            description = "İki aracı ihtiyaçlara göre karşılaştırır.",
            promptText = "[ARAÇ 1] ve [ARAÇ 2] modellerini yakıt, bakım, konfor ve ikinci el değeri açısından karşılaştır.",
            usageCount = 120
        )
    )
}
