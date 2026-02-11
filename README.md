# Prompt Bankası (Android)

Prompt Bankası, tamamen Türkçe arayüze sahip, hazır yapay zeka promptlarını kategori bazlı sunan bir Android uygulamasıdır.

## Özellikler

- Modern ve sade ana ekran
- Kategori kartları ve arama çubuğu
- En popüler promptlar
- Prompt detay sayfası
  - Başlık
  - Açıklama
  - Prompt metni
  - Kopyala
  - Favorilere ekle
  - Paylaş
- AI ile otomatik prompt oluşturucu (şablon tabanlı)
- Kullanıcının kendi promptlarını ekleyebilmesi
- Karanlık / Aydınlık tema desteği
- Favoriler bölümü
- Günün promptu
- En çok kullanılan promptlar
- Günün promptu için bildirim altyapısı (WorkManager)
- Offline kullanım (veriler cihazda saklanır)
- İletişim bilgileri
  - Burak Demir
  - burakk.demirr.02@gmail.com

## Kategoriler

- 📚 Eğitim
- 💼 İş & Kariyer
- 📱 Sosyal Medya
- ✍️ İçerik Üretimi
- 💻 Kodlama
- 🎯 Pazarlama
- 🎨 Tasarım
- 🧠 Kişisel Gelişim
- 🧑‍⚖️ Resmi Yazışma
- 🧩 Eğlenceli Promptlar
- 🚗 Araba

## Proje Yapısı

- `app/src/main/java/com/promptbankasi/MainActivity.kt`: Uygulama ekranları ve gezinme yapısı.
- `app/src/main/java/com/promptbankasi/ui/PromptViewModel.kt`: UI state, favoriler, tema ve local kayıt işlemleri.
- `app/src/main/java/com/promptbankasi/data/PromptRepository.kt`: Kategori ve örnek prompt verileri.
- `app/src/main/java/com/promptbankasi/notifications/NotificationScheduler.kt`: Günlük bildirim planlama ve gönderim.

## Çalıştırma

1. Android Studio ile projeyi açın.
2. Gradle senkronizasyonunu tamamlayın.
3. Bir emulator veya fiziksel cihaz seçip uygulamayı başlatın.

