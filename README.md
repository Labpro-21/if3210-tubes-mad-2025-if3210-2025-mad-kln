<h1 align="center">
Purrytify App
</h1>

> Repository Tugas Besar 1 Pengembangan Aplikasi Piranti Bergerak 2025 - Kelompok KLN

<p align="justify"> 
Pada tugas besar 1, kami diminta untuk membuat sebuah aplikasi music player seperti Spotify menggunakan Android Native dengan bahasa Kotlin. Fitur utama dari aplikasi ini adalah login, home page, library page, music player page, dan profile page. Setiap fitur memiliki fungsionalitas yang berbeda, seperti pada halaman login, pengguna dapat masuk dengan akun mereka, sedangkan di halaman home, pengguna dapat melihat lagu terbaru. Di halaman library, pengguna bisa mengelola lagu favorit mereka, dan pada music player page, mereka dapat memutar, menjeda, dan mengontrol musik yang sedang diputar. Terakhir, di halaman profile, pengguna dapat melihat informasi mengenai akun mereka dan menyunting foto profil serta lokasi. Selain itu, aplikasi ini juga menyediakan fungsionalitas untuk mengunggah lagu, mencari lagu berdasarkan judul atau artist, serta menyimpan preferensi musik pengguna. 
</p>

<p align="center">
    <img src="screenshot/banner.png" alt="Banner Screenshot" style="width: 100%; height: auto; object-fit: contain;">
</p>

## Spesifikasi

<table border="1">
  <tr>
    <td>Bahasa Pemrograman</td>
    <td>Kotlin (Android Native)</td>
  </tr>
  <tr>
    <td>Namespace</td>
    <td><code>com.android.purrytify</code></td>
  </tr>
  <tr>
    <td>compileSdk</td>
    <td>34</td>
  </tr>
  <tr>
    <td>applicationId</td>
    <td><code>com.android.purrytify</code></td>
  </tr>
  <tr>
    <td>minSdk</td>
    <td>29</td>
  </tr>
  <tr>
    <td>targetSdk</td>
    <td>34</td>
  </tr>
</table>

## Library Yang Digunakan

1. Library Inti Android <br>
   `androidx.core:core-ktx`
   `androidx.lifecycle:lifecycle-runtime-ktx`
   `androidx.activity:activity-compose` <br> <br>
2. Jetpack Compose (UI Deklaratif Android) <br>
   `androidx.compose:compose-bom`
   `androidx.compose.ui:ui`
   `androidx.compose.ui:ui-graphics`
   `androidx.compose.ui:ui-tooling-preview`
   `androidx.compose.material3:material3`
   `androidx.navigation:navigation-compose`
   `androidx.compose.material:material-icons-extended` <br><br>
3. Accompanist (Tambahan untuk Compose) <br>
   `accompanist-systemuicontroller`
   `accompanist-navigation-animation`
   `androidx.palette:palette-ktx` <br> <br>
4. RecyclerView & Material <br>
   `androidx.recyclerview:recyclerview`
   `androidx.cardview:cardview`
   `com.google.android.material:material` <br> <br>
5. Room Database (Penyimpanan Lokal) <br>
   `androidx.room:room-runtime`
   `androidx.room:room-compiler`
   `androidx.room:room-ktx` <br> <br>
6. Lifecycle & ViewModel <br>
   `androidx.lifecycle:lifecycle.viewmodel.android`
   `lifecycle-runtime-compose`
   `lifecycle-viewmodel-compose`
   `lifecycle-viewmodel-ktx`
   `lifecycle-livedata-ktx` <br> <br>
7. Coroutines <br>
   `kotlinx-coroutines-core`
   `kotlinx-coroutines-android` <br> <br>
8. Coil (Pemrosesan Gambar) <br>
   `coil-compose`<br> <br>
9. Splash Screen <br>
   `core-splashscreen` <br> <br>
10. Login dan Networking <br>
    `retrofit`
    `converter-gson`
    `okhttp3:logging-interceptor`
    `datastore-preferences` <br> <br>

## Halaman

<table style="width: 100%;" border="1">
    <tr>
        <th>No</th>
        <th>Halaman</th>
        <th>Screenshot</th>
        <th>No</th>
        <th>Halaman</th>
        <th>Screenshot</th>
    </tr>
    <tr>
        <td>1</td>
        <td>Splash</td>
        <td style="text-align: center;">
            <img src="screenshot/splash.png" alt="Screenshot Splash" style="width: 150px; height: auto; object-fit: contain;">
        </td>
        <td>2</td>
        <td>Login</td>
        <td style="text-align: center;">
            <img src="screenshot/login.jpg" alt="Screenshot Login" style="width: 150px; height: auto; object-fit: contain;">
        </td>
    </tr>
    <tr>
        <td>3</td>
        <td>Home</td>
        <td style="text-align: center;">
            <img src="screenshot/home.jpg" alt="Screenshot Home" style="width: 150px; height: auto; object-fit: contain;">
        </td>
        <td>4</td>
        <td>Home (Responsive)</td>
        <td style="text-align: center;">
            <img src="screenshot/home_exampleresponsive.jpg" alt="Screenshot Home Responsive" style="width: 150px; height: auto; object-fit: contain;">
        </td>
    </tr>
    <tr>
        <td>5</td>
        <td>Library</td>
        <td style="text-align: center;">
            <img src="screenshot/library.jpg" alt="Screenshot Library" style="width: 150px; height: auto; object-fit: contain;">
        </td>
        <td>6</td>
        <td>Now Playing</td>
        <td style="text-align: center;">
            <img src="screenshot/nowplaying.jpg" alt="Screenshot Now Playing" style="width: 150px; height: auto; object-fit: contain;">
        </td>
    </tr>
    <tr>
        <td>7</td>
        <td>Upload Song</td>
        <td style="text-align: center;">
            <img src="screenshot/uploadsong.jpg" alt="Screenshot Upload Song" style="width: 150px; height: auto; object-fit: contain;">
        </td>
        <td>8</td>
        <td>Edit Song</td>
        <td style="text-align: center;">
            <img src="screenshot/editsong.jpg" alt="Screenshot Edit Song" style="width: 150px; height: auto; object-fit: contain;">
        </td>
    </tr>
    <tr>
        <td>9</td>
        <td>Profile</td>
        <td style="text-align: center;">
            <img src="screenshot/profile.jpg" alt="Screenshot Profile" style="width: 150px; height: auto; object-fit: contain;">
        </td>
        <td>10</td>
        <td>Edit Profile</td>
        <td style="text-align: center;">
            <img src="screenshot/profile_edit.jpg" alt="Screenshot Edit Profile" style="width: 150px; height: auto; object-fit: contain;">
        </td>
    </tr>
    <tr>
        <td>11</td>
        <td>Chart - For You</td>
        <td style="text-align: center;">
            <img src="screenshot/chart_foryou.jpg" alt="Screenshot Chart For You" style="width: 150px; height: auto; object-fit: contain;">
        </td>
        <td>12</td>
        <td>Chart - Top Global</td>
        <td style="text-align: center;">
            <img src="screenshot/chart_topglobal.jpg" alt="Screenshot Chart Top Global" style="width: 150px; height: auto; object-fit: contain;">
        </td>
    </tr>
    <tr>
        <td>13</td>
        <td>Chart - Top Country</td>
        <td style="text-align: center;">
            <img src="screenshot/chart_topcountry.jpg" alt="Screenshot Chart Top Country" style="width: 150px; height: auto; object-fit: contain;">
        </td>
        <td>14</td>
        <td>Scan QR</td>
        <td style="text-align: center;">
            <img src="screenshot/scanqr.jpg" alt="Screenshot Scan QR" style="width: 150px; height: auto; object-fit: contain;">
        </td>
    </tr>
    <tr>
        <td>15</td>
        <td>SoundCapsule - Summary</td>
        <td style="text-align: center;">
            <img src="screenshot/soundcapsule.jpg" alt="Screenshot SoundCapsule" style="width: 150px; height: auto; object-fit: contain;">
        </td>
        <td>16</td>
        <td>SoundCapsule - Top Songs</td>
        <td style="text-align: center;">
            <img src="screenshot/soundcapsule_topsongs.jpg" alt="Screenshot SoundCapsule Top Songs" style="width: 150px; height: auto; object-fit: contain;">
        </td>
    </tr>
    <tr>
        <td>17</td>
        <td>SoundCapsule - Top Artists</td>
        <td style="text-align: center;">
            <img src="screenshot/soundcapsule_topartists.jpg" alt="Screenshot SoundCapsule Top Artists" style="width: 150px; height: auto; object-fit: contain;">
        </td>
        <td>18</td>
        <td>SoundCapsule - Time Listened</td>
        <td style="text-align: center;">
            <img src="screenshot/soundcapsule-timelistened.jpg" alt="Screenshot SoundCapsule Time Listened" style="width: 150px; height: auto; object-fit: contain;">
        </td>
    </tr>
    <tr>
        <td>19</td>
        <td>No Internet</td>
        <td style="text-align: center;">
            <img src="screenshot/nointernet.png" alt="Screenshot No Internet" style="width: 150px; height: auto; object-fit: contain;">
        </td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
</table>

## Bonus yang dikerjakan

1. Shuffle
2. Repeat
3. Pencarian

## Pembagian Kerja Milestone 1

[Google Docs: Detail Pembagian Kerja](https://docs.google.com/document/d/1nY7Lu_uelhO5CIBwzEW72Fe1I4ItoQklNpqjB6kYFIU/edit?usp=sharing)

<table border="1">
    <tr>
        <th>No</th>
        <th>Nama</th>
        <th>NIM</th>
        <th>Pembagian Tugas</th>
        <th>Waktu Persiapan</th>
        <th>Jam Kerja</th>
    </tr>
    <tr>
        <td>1</td>
        <td>Maulana Muhammad Susetyo</td>
        <td>13522127</td>
        <td>Logic Media Player, Miniplayer, Track View Page, FE Profile Page, Repeat, Shuffle</td>
        <td>8</td>
        <td>28</td>
    </tr>
    <tr>
        <td>2</td>
        <td>Ahmad Rafi Maliki</td>
        <td>13522137</td>
        <td>Database, Splash Screen, Navbar, Homepage, Library Page, Upload/Edit/Delete, Network Sensing, Search</td>
        <td>10</td>
        <td>40</td>
    </tr>
    <tr>
        <td>3</td>
        <td>Andi Marihot Sitorus</td>
        <td>13522138</td>
        <td>Login, Background Service, Profile BE</td>
        <td>5</td>
        <td>20</td>
    </tr>
</table>

## Pembagian Kerja Milestone 2

[Google Docs: Detail Pembagian Kerja](https://docs.google.com/document/d/191Kz_m-H4IY3DZO7CByAxx7rnbM-LiQlyvRSJEGWE_o/edit?usp=sharing)

<table border="1">
    <tr>
        <th>No</th>
        <th>Nama</th>
        <th>NIM</th>
        <th>Pembagian Tugas</th>
        <th>Waktu Persiapan</th>
        <th>Jam Kerja</th>
    </tr>
    <tr>
        <td>1</td>
        <td>Maulana Muhammad Susetyo</td>
        <td>13522127</td>
        <td>Notification, Audio Routing, Share Song QR, Database Logging, Export to CSV sound capsule</td>
        <td>0</td>
        <td>45</td>
    </tr>
    <tr>
        <td>2</td>
        <td>Ahmad Rafi Maliki</td>
        <td>13522137</td>
        <td>Online Songs, Download Online Songs, Sound Capsule UI, Share Song URL, Responsive Page, Song Recommendation</td>
        <td>0</td>
        <td>45</td>
    </tr>
    <tr>
        <td>3</td>
        <td>Andi Marihot Sitorus</td>
        <td>13522138</td>
        <td>Edit Profil</td>
        <td>0</td>
        <td>10</td>
    </tr>
</table>
