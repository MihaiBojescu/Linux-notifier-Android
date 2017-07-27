package dev.mihaibojescu.linuxnotifier.UiTools;

import android.view.View;

public interface ClickListener {
    void onClick(View view, int position);
    void onLongClick(View view, int position);
}
