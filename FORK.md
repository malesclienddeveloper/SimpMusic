# SimpWave — fork SimpMusic bergaya Spotify

Fork ini mengubah tampilan SimpMusic (tema hitam + hijau, bottom nav, Home, Library, mini player, lirik)
dan memakai identitas sendiri (`app.simpwave.music`), sehingga **terpasang berdampingan** dengan SimpMusic
asli dan **hanya update dari rilis repo Anda**. Semua perubahan fork ditandai komentar `FORK:`.

## Setup sekali saja
1. **Fork** `maxrave-dev/SimpMusic` ke akun Anda, lalu terapkan patch ini:
   `git am 0001-*.patch` (atau `git apply`), lalu push ke branch default fork.
2. **Buat kunci tanda tangan** (simpan file `.jks` baik-baik; kalau hilang, update tidak bisa dipasang di atas versi lama):
   ```
   keytool -genkeypair -v -keystore release.jks -alias simpwave -keyalg RSA -keysize 2048 -validity 10000
   base64 -w0 release.jks     # macOS: base64 -i release.jks
   ```
3. Di GitHub: *Settings → Secrets and variables → Actions*, tambah secret
   `SIGNING_KEY` (hasil base64), `ALIAS`, `KEY_STORE_PASSWORD`, `KEY_PASSWORD`.
4. *Settings → Actions → General → Workflow permissions*: pilih **Read and write** dan centang
   **Allow GitHub Actions to create and approve pull requests**.
5. (Disarankan) buat Personal Access Token dengan scope `repo` + `workflow`, simpan sebagai secret `SYNC_TOKEN`.
   Tanpanya sinkron upstream bisa ditolak GitHub bila upstream mengubah file workflow.

## Rilis APK
```
git tag v1.0.0 && git push origin v1.0.0
```
Workflow **Build & release APK** membangun APK FOSS (tanpa Sentry/tracking), menandatangani, lalu
menempelkannya di GitHub Release. Nomor versi diambil dari tag. Bisa juga dijalankan manual dari tab Actions.
Aplikasi mengecek `releases/latest` repo Anda dan menawarkan update bila tag-nya lebih baru.

## Update dari SimpMusic asli
Workflow **Sync upstream SimpMusic** jalan tiap Senin dan membuka Pull Request `upstream-sync`.
Konflik (biasanya di file UI yang ditandai `FORK:`) terlihat di PR atau dibuat sebagai Issue; selesaikan,
merge, lalu beri tag rilis baru.

## Catatan
- Build lokal tanpa CI: updater nonaktif (placeholder repo belum diisi) — normal.
- Layar "build tidak resmi" milik upstream dimatikan karena fork punya package dan kunci sendiri.
- Lisensi GPL-3.0 tetap berlaku: sertakan sumber kode fork Anda dan kredit ke SimpMusic.
- Nama "Spotify" dan logonya tidak dipakai; hanya pola tata letak dan warna umum.
