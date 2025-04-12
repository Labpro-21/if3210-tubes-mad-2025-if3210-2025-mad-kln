# if3210-tubes-mad-2025-if3210-2025-mad-kln

> Repository Tugas Besar 1 Mobile App Development 2025

<p align="justify"> 
Pada tugas besar 1, kami diminta untuk membuat sebuah aplikasi music player seperti Spotify menggunakan Android Native dengan bahasa Kotlin. Fitur utama dari aplikasi ini adalah login, home page, library page, music player page, dan profile page. Setiap fitur memiliki fungsionalitas yang berbeda, seperti pada halaman login, pengguna dapat masuk dengan akun mereka, sedangkan di halaman home, pengguna dapat melihat lagu terbaru. Di halaman library, pengguna bisa mengelola lagu favorit mereka, dan pada music player page, mereka dapat memutar, menjeda, dan mengontrol musik yang sedang diputar. Terakhir, di halaman profile, pengguna dapat melihat informasi mengenai akun mereka. Selain itu, aplikasi ini juga menyediakan fungsionalitas untuk mengunggah lagu, mencari lagu berdasarkan judul, serta menyimpan preferensi musik pengguna. 
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
   `coil-compose`
9. Splash Screen <br>
   `core-splashscreen` <br> <br>
10. Login dan Networking <br>
    `retrofit`
    `converter-gson`
    `okhttp3:logging-interceptor`
    `datastore-preferences` <br> <br>

## Screenshot

<table style="width: 100%;" border="1">
    <tr>
        <th>No</th>
        <th>Halaman</th>
        <th>Screenshot</th>
    </tr>
    <tr>
        <td>1</td>
        <td>Login Page</td>
        <td style="text-align: center;">
            <img src="screenshot/login.png" alt="Screenshot Login Page" style="max-width: 150; height: auto;">
        </td>
    </tr>
    <tr>
        <td>2</td>
        <td>Home Page</td>
        <td style="text-align: center;">
            <img src="screenshot/home.png" alt="Screenshot Home Page" style="max-width: 150; height: auto;">
        </td>
    </tr>
    <tr>
        <td>3</td>
        <td>Library Page</td>
        <td style="text-align: center;">
            <img src="screenshot/library.png" alt="Screenshot Library Page" style="max-width: 150; height: auto;">
        </td>
    </tr>
    <tr>
        <td>4</td>
        <td>Track View Page</td>
        <td style="text-align: center;">
            <img src="screenshot/player.png" alt="Screenshot Music Player Page" style="max-width: 150; height: auto;">
        </td>
    </tr>
    <tr>
        <td>5</td>
        <td>Profile Page</td>
        <td style="text-align: center;">
            <img src="screenshot/profile.png" alt="Screenshot Profile Page" style="max-width: 150; height: auto;">
        </td>
    </tr>
    <tr>
        <td>6</td>
        <td>No Internet</td>
        <td style="text-align: center;">
            <img src="screenshot/nointernet.png" alt="Screenshot Profile Page" style="max-width: 150; height: auto;">
        </td>
    </tr>
    <tr>
        <td>7</td>
        <td>Upload Song</td>
        <td style="text-align: center;">
            <img src="screenshot/upload.png" alt="Screenshot Profile Page" style="max-width: 150; height: auto;">
        </td>
    </tr>
</table>

## Pembagian Kerja

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
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>2</td>
        <td>Ahmad Rafi Maliki</td>
        <td>13522137</td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>3</td>
        <td>Andi Marihot Sitorus</td>
        <td>13522138</td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
</table>
