package dev.mihaibojescu.linuxnotifier.Runnables;

import dev.mihaibojescu.linuxnotifier.MainActivity;

/**
 * Created by michael on 8/13/17.
 */

public class UpdateUIRunnable implements  Runnable
{
    private MainActivity mainActivity;


    public UpdateUIRunnable(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run()
    {
        this.mainActivity.getAdapter().notifyDataSetChanged();
    }
}


