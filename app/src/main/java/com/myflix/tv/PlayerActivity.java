package com.myflix.tv;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class PlayerActivity extends Activity {
    private ExoPlayer player;
    private PlayerView playerView;
    private TextView status;
    private String url;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        immersive();

        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        playerView = new PlayerView(this);
        playerView.setUseController(true);
        root.addView(playerView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        status = new TextView(this);
        status.setTextColor(Color.WHITE);
        status.setTextSize(13);
        status.setPadding(20, 14, 20, 14);
        status.setBackgroundColor(0x99000000);
        status.setText(title == null || title.isEmpty() ? "MyFlix" : title);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP);
        root.addView(status, sp);

        setContentView(root);

        if (url == null || url.trim().isEmpty()
                || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            Toast.makeText(this, "Không có link M3U8 hợp lệ", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    status.setText((title == null ? "MyFlix" : title) + "\nĐang tải...");
                    status.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    status.setText(title == null ? "MyFlix" : title);
                    status.postDelayed(() -> status.setVisibility(View.GONE), 1800);
                } else if (playbackState == Player.STATE_ENDED) {
                    setResult(RESULT_OK);
                    finish();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                status.setVisibility(View.VISIBLE);
                String msg = error.getErrorCodeName();
                status.setText("Không phát được video\n" + msg + "\n" + url);
                Toast.makeText(PlayerActivity.this, "Media3: " + msg, Toast.LENGTH_LONG).show();
            }
        });

        // Giống hệt APK test đã phát thành công: đưa URL M3U8 trực tiếp vào Media3.
        player.setMediaItem(MediaItem.fromUri(Uri.parse(url)));
        player.prepare();
        player.play();
        playerView.requestFocus();
    }

    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction() != KeyEvent.ACTION_DOWN || player == null) {
            return super.dispatchKeyEvent(e);
        }
        switch (e.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (player.isPlaying()) player.pause(); else player.play();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                player.seekTo(Math.max(0, player.getCurrentPosition() - 10000));
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                player.seekTo(player.getCurrentPosition() + 10000);
                return true;
            default:
                return super.dispatchKeyEvent(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        immersive();
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
