# T&M Herbal

Aplikasi Android (Kotlin + Jetpack Compose) untuk arsip tanaman dan minuman herbal. Pengguna dapat mencari herbal dengan penelusuran DFS, membaca detail khasiat dan cara pengolahan, serta menyimpan favorit. Admin dapat mengelola data herbal beserta fotonya.

## Fitur

- Autentikasi email/password (Firebase Authentication), termasuk mode tamu.
- Pencarian herbal berbasis DFS dengan metrik hasil, jumlah node, dan waktu proses.
- Daftar herbal, detail khasiat/takaran/gejala, dan favorit.
- Panel admin untuk tambah, ubah, dan hapus data herbal.
- Unggah foto herbal otomatis saat data disimpan.

## Persyaratan

- Android Studio (versi yang mendukung AGP 8.7.3).
- JDK 17.
- Android SDK 35.
- Perangkat atau emulator Android 7.0 (API 24) ke atas.

## Konfigurasi Firebase

1. Buat project di Firebase Console.
2. Tambahkan aplikasi Android dengan package `com.akacaraka.herbal`.
3. Unduh `google-services.json` dan letakkan di `app/google-services.json`.
4. Aktifkan **Authentication → Sign-in method → Email/Password**.
5. Aktifkan **Cloud Firestore**.

### Struktur data Firestore

Koleksi `herbal_items` menyimpan data herbal dengan field:

```text
id, category, localName, scientificName, description, benefits, dosage, symptoms, imageUrl
```

Nilai `category` adalah `TANAMAN` atau `MINUMAN`.

Koleksi `admins` menentukan akun mana yang punya akses admin. Buat dokumen dengan ID sama persis dengan UID akun Firebase Authentication yang ingin dijadikan admin.

### Contoh aturan keamanan Firestore

```js
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /admins/{userId} {
      allow get: if request.auth != null && request.auth.uid == userId;
    }

    match /herbal_items/{itemId} {
      allow read: if true;
      allow create, update, delete: if request.auth != null
        && exists(/databases/$(database)/documents/admins/$(request.auth.uid));
    }
  }
}
```

## Penyimpanan foto

Foto herbal diunggah ke object storage berbasis HTTP dan URL hasilnya disimpan pada field `imageUrl`. Alamat bucket diatur pada konstanta di `app/src/main/java/com/akacaraka/herbal/firebase/HerbalRepository.kt`. Sebelum rilis produksi, batasi izin tulis bucket agar tidak dapat diakses publik.

## Menjalankan dari Android Studio

1. Pilih **Open**, lalu arahkan ke folder project ini.
2. Setel Gradle JDK ke versi 17 melalui **Settings → Build, Execution, Deployment → Build Tools → Gradle**.
3. Tunggu proses Gradle sync selesai.
4. Pilih perangkat atau emulator, lalu jalankan konfigurasi `app`.

## Menjalankan dari terminal

Build APK debug:

```bash
./gradlew :app:assembleDebug
```

Pasang ke perangkat yang terhubung:

```bash
./gradlew :app:installDebug
```

Hasil APK debug berada di:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Catatan

- Seluruh data herbal berasal dari Firestore. Jika koleksi `herbal_items` kosong, aplikasi menampilkan kondisi kosong tanpa data contoh bawaan.
- Berkas `local.properties` bersifat lokal dan berisi lokasi Android SDK pada mesin masing-masing.
