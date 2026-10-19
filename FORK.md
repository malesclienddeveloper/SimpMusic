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

## Penghalusan UI (v1.0.1)

File baru (logika di luar file upstream, supaya merge upstream mudah):
- `ui/component/SpotifyLyricsCard.kt` — kartu lirik di bawah artwork (baris aktif + berikutnya, ketuk = lirik layar penuh). Dipakai oleh `NowPlayingContentSpotify.kt` bila celah di bawah artwork >= 72dp.
- `ui/screen/home/SpotifyHistoryHeader.kt` — header riwayat dengan tombol play hijau.
- `SpotifyStyle` — token spasi/radius/garis tipis (`screenPadding`, `radiusCard`, `hairline`, dst).

Diubah (ditandai `FORK:`): `LibraryTilingBox.kt` (daftar baris, bukan kartu pastel), `RecentlySongsScreen.kt` (header), `MiniPlayer.kt` (radius 12dp + garis tipis), `NowPlayingContentSpotify.kt` (kartu lirik).

## Login Google via akun perangkat + polesan Apple Music (v1.0.2)

- `expect/ui/GoogleAccountPicker*.kt`: pemilih akun Google bawaan perangkat (Android: `AccountManager.newChooseAccountIntent`, tanpa izin; Desktop: null).
- `ui/screen/login/GoogleAccountChooser.kt` + `LogInScreen.kt`: layar pilih akun sebelum WebView; email dikirim sebagai `Email`/`login_hint` tanpa `passive=true` agar Google menampilkan verifikasi akun secara interaktif. Deteksi login di `PlatformWebView` mendukung variasi URL tujuan (query params seperti `?pli=1`) dan memastikan cookie autentikasi (`SAPISID`, dsb.) sudah terisi sebelum menyimpan akun. Tombol kembali dapat kembali ke pemilih akun.
- `expect/ui/Cookies.android.kt`: `PlatformWebView` dilengkapi third-party cookies, penghapusan identifier WebView pada User-Agent agar tidak terblokir `403 disallowed_useragent`, penanganan skema `intent://`, serta sinkronisasi cookie.
- `AppleMusicShared.kt`: baris lirik tunggal (`AppleMusicLyricStrip`, sudah berganti per baris) dibuat lebih besar/tebal.

## Bantuan login saat Google memblokir WebView (v1.0.3)

Kebijakan "secure browsers" Google menolak login dari WebView mana pun, dan itu tidak bisa ditipu lewat User-Agent: WebView tetap mengirim `Sec-CH-UA: "Android WebView"` dan `X-Requested-With: <nama paket>` ke Google. Jalur yang terbukti bekerja adalah jalur impor cookie (sama seperti alur resmi Desktop SimpMusic).

- `ui/screen/login/LoginHelpSheet.kt` (baru) — dialog bantuan: muncul otomatis saat WebView mendarat di halaman penolakan Google (`accounts.google.com/.../signin/rejected`), juga bisa dibuka dari ikon `?` di toolbar login. Tombol: **Impor cookie** (membuka lembar tempel cookie), **Coba lagi** (membangun ulang WebView), **Tutup**.
- `LogInScreen.kt` — deteksi `isGoogleSignInBlocked(url)` pada `onPageFinished`; "Coba lagi" memakai `key(reloadWebView)` untuk membangun ulang `PlatformWebView`; tempelan cookie dibersihkan oleh `cleanPastedCookie()` (menerima awalan `Cookie:`, spasi, dan baris baru).
- Cara login yang bekerja: masuk ke YouTube Music di browser asli (komputer: Chrome + ekstensi **SimpMusic Utils**; Android: browser Chromium yang mendukung ekstensi), salin cookie YouTube Music, lalu tempel lewat **Impor cookie** di layar login.
