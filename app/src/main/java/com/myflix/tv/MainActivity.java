package com.myflix.tv;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class MainActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView playerView;
    private EditText urlInput;
    private TextView statusText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.playerView);
        urlInput = findViewById(R.id.urlInput);
        statusText = findViewById(R.id.statusText);
        Button playButton = findViewById(R.id.playButton);
        Button stopButton = findViewById(R.id.stopButton);

        initPlayer();

        playButton.setOnClickListener(v -> playUrl());
        stopButton.setOnClickListener(v -> {
            if (player != null) {
                player.stop();
                player.clearMediaItems();
                statusText.setText("Đã dừng");
            }
        });
    }

    private void initPlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    statusText.setText("Đang tải...");
                } else if (playbackState == Player.STATE_READY) {
                    statusText.setText("Đang phát");
                } else if (playbackState == Player.STATE_ENDED) {
                    statusText.setText("Đã phát xong");
                } else if (playbackState == Player.STATE_IDLE) {
                    statusText.setText("Sẵn sàng");
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                String msg = error.getMessage();
                if (msg == null || msg.trim().isEmpty()) {
                    msg = "Không phát được video";
                }
                statusText.setText("Lỗi: " + msg);
                Toast.makeText(MainActivity.this, "Media3: " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void playUrl() {
        String url = urlInput.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "Anh dán link .m3u8 hoặc video trước", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(this, "Link phải bắt đầu bằng http:// hoặc https://", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
            statusText.setText("Đang mở link...");
        } catch (Exception e) {
            statusText.setText("Lỗi: " + e.getMessage());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}
