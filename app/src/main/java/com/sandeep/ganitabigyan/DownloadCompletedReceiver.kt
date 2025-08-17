package com.sandeep.ganitabigyan

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * ଯେତେବେଳେ DownloadManager ଏକ ଫାଇଲ୍ ଡାଉନଲୋଡ୍ କରିବା ଶେଷ କରେ,
 * ସେତେବେଳେ ଏହି BroadcastReceiver ସିଷ୍ଟମ୍ ଦ୍ୱାରା ଚଲାଯାଏ ।
 * ଏହାର ମୁଖ୍ୟ କାମ ହେଉଛି ଡାଉନଲୋଡ୍ ହୋଇଥିବା APK ଫାଇଲକୁ ଇନଷ୍ଟଲ୍ କରିବା ପାଇଁ ପ୍ରମ୍ପ୍ଟ ଦେବା ।
 */
class DownloadCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // କେବଳ ACTION_DOWNLOAD_COMPLETE ଇଭେଣ୍ଟ ପାଇଁ କାମ କରନ୍ତୁ
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {

            // ସମାପ୍ତ ହୋଇଥିବା ଡାଉନଲୋଡର ID ପ୍ରାପ୍ତ କରନ୍ତୁ
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == -1L) {
                return
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            // ଡାଉନଲୋଡ୍ ହୋଇଥିବା ଫାଇଲର URI (ঠিকଣା) ପ୍ରାପ୍ତ କରନ୍ତୁ
            val fileUri = downloadManager.getUriForDownloadedFile(id)

            if (fileUri != null) {
                // ଯଦି ଫାଇଲ୍ URI ମିଳିଗଲା, ତେବେ ଏକ ଇନଷ୍ଟଲେସନ୍ ଇଣ୍ଟେଣ୍ଟ ତିଆରି କରନ୍ତୁ
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    // ଇନଷ୍ଟଲେସନ୍ ପ୍ରକ୍ରିୟା ଆରମ୍ଭ କରିବା ପାଇଁ ଇଣ୍ଟେଣ୍ଟକୁ ଚଲାନ୍ତୁ
                    context.startActivity(installIntent)
                } catch (e: Exception) {
                    // ଯଦି କୌଣସି ତ୍ରୁଟି ହୁଏ, ତେବେ ଏକ ଟୋଷ୍ଟ୍ ମେସେଜ୍ ଦେଖାନ୍ତୁ
                    Toast.makeText(context, "ଇନଷ୍ଟଲର୍ ଆରମ୍ଭ କରିବାରେ ବିଫଳ ହେଲା ।", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            } else {
                // ଯଦି ଫାଇଲ୍ URI ମିଳିଲା ନାହିଁ, ତେବେ ଡାଉନଲୋଡ୍ ବିଫଳ ହେବାର ଏକ ଟୋଷ୍ଟ୍ ମେସେଜ୍ ଦେଖାନ୍ତୁ
                Toast.makeText(context, "ଡାଉନଲୋଡ୍ ବିଫଳ ହେଲା ।", Toast.LENGTH_LONG).show()
            }
        }
    }
}