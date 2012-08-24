package net.garrapeta.gameengine.sound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Manager de samples de sonido
 * 
 * @author GaRRaPeTa
 */
public class SoundManager {

    // -------------------------------- Variables est�ticas

    /** Source trazas de log */
    public static final String LOG_SRC = "sound";

    // ----------------------------- Variables de instancia

    private HashMap<Integer, PlayerSet> playerSets;
    
    private boolean mSoundEnabled = true;

    // ----------------------------------------------- Constructor

    /**
     * Constructor protegido
     */
    public SoundManager() {
        playerSets = new HashMap<Integer, PlayerSet>();
    }

    // ------------------------------------------ M�todos de instancia

    /**
     * @return if the sound is enabled
     */
    public final boolean isSoundEnabled() {
        return mSoundEnabled;
    }

    /**
     * Sets whether or not the sound is enabled
     * @param soundEnabled
     */
    public final void setSoundEnabled(boolean soundEnabled) {
        mSoundEnabled = soundEnabled;
    }

    /**
     * Borra todo
     * 
     * @param context
     */
    public void clearAll() {
        Log.i(LOG_SRC, "disposing");
        stopAll();
        releaseAll();
        playerSets.clear();
    }

    /**
     * A�ade un sample
     * 
     * @param resourceId
     * @param sampleId
     */
    public void add(int resourceId, int sampleId, Context context) {
        MediaPlayer player = MediaPlayer.create(context, resourceId);
        /*
         * try { player.prepare(); } catch (IOException ioe) {
         * ioe.printStackTrace(); String msg = "Poblems adding sampleId " +
         * ioe.toString(); Log.e(LOG_SRC, msg); throw new
         * IllegalArgumentException(msg); }
         */

        PlayerSet set;

        if (playerSets.containsKey(sampleId)) {
            set = playerSets.get(sampleId);
        } else {
            set = new PlayerSet();
            playerSets.put(sampleId, set);
        }

        set.add(player);
    }

    /**
     * Reproduce uno de los samples identificados con sampleId
     * 
     * @param sampleId
     */
    public MediaPlayer play(int sampleId) {
        return play(sampleId, false, true);
    }

    /**
     * Reproduce uno de los samples identificados con sampleId
     * 
     * @param sampleId
     */
    public MediaPlayer play(int sampleId, boolean loop, boolean reset) {
        if (!mSoundEnabled) {
            return null;
        }
        if (playerSets.containsKey(sampleId)) {
            PlayerSet set = playerSets.get(sampleId);
            MediaPlayer p = set.play(loop, reset);
            Log.d(LOG_SRC, "playing: " + sampleId + " " + p);
            return p;
        } else {
            throw new IllegalArgumentException("No such sample id: " + sampleId);
        }
    }

    /**
     * Pausa el player
     */
    public void pause(MediaPlayer p) {
        Log.d(LOG_SRC, "pause: " + p);
        if (p.isPlaying()) {
            p.pause();
        }
    }

    /**
     * Resume el player
     */
    public void resume(MediaPlayer p) {
        Log.d(LOG_SRC, "resume: " + p);
        if (!p.isPlaying() && p.getCurrentPosition() != 0) {
            p.start();
        }
    }

    /**
     * Para el player
     */
    public void stop(MediaPlayer p) {
        Log.d(LOG_SRC, "stop: " + p);
        if (p.isPlaying()) {
            p.stop();
            try {
                p.prepare();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                String msg = "Poblems stopping sampleId " + ioe.toString();
                Log.e(LOG_SRC, msg);
                throw new IllegalArgumentException(msg);
            }
        }
    }

    /**
     * Para todos los reproductores
     */
    public void stopAll() {
        Iterator<Entry<Integer, PlayerSet>> it = playerSets.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, PlayerSet> entry = it.next();
            PlayerSet playerSet = entry.getValue();
            playerSet.stopAll();
        }
    }

    /**
     * Pause todos los reproductores
     */
    public void pauseAll() {
        Iterator<Entry<Integer, PlayerSet>> it = playerSets.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, PlayerSet> entry = it.next();
            PlayerSet playerSet = entry.getValue();
            playerSet.pauseAll();
        }
    }

    /**
     * Resume todos los reproductores
     */
    public void resumeAll() {
        Iterator<Entry<Integer, PlayerSet>> it = playerSets.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, PlayerSet> entry = it.next();
            PlayerSet playerSet = entry.getValue();
            playerSet.resumeAll();
        }
    }

    /**
     * Disposea todos los players
     */
    private void releaseAll() {
        Iterator<Entry<Integer, PlayerSet>> it = playerSets.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, PlayerSet> entry = it.next();
            PlayerSet playerSet = entry.getValue();
            playerSet.releaseAll();
        }
    }

    /**
     * M�todo que encapsula diferentes players que pueden estar referenciados
     * por un mismo id
     * 
     * @author GaRRaPeTa
     */
    class PlayerSet {

        // ----------------------------- Variables de instancia

        private MediaPlayer player = null;
        private ArrayList<MediaPlayer> players = null;

        // ----------------------------- M�todos

        /**
         * A�ade un player
         * 
         * @param sample
         */
        void add(MediaPlayer sample) {
            if (player == null && players == null) {

                player = sample;
            } else {

                if (players == null) {
                    players = new ArrayList<MediaPlayer>();
                    player = null;
                }
                players.add(sample);
            }

            // sample.prepareAsync();
        }

        /**
         * Hace sonar uno de los players de este PlayerSet
         */
        MediaPlayer play(boolean loop, boolean reset) {
            MediaPlayer p = null;
            if (player != null) {
                p = player;
            } else {
                int index = (int) Math.floor(Math.random() * players.size());
                p = players.get(index);
            }

            if (p.isPlaying()) {
                if (reset) {
                    p.seekTo(0);
                } else {
                    return p;
                }
            }

            p.setLooping(loop);
            p.start();

            return p;
        }

        /**
         * Pausa todos los reproductores de este set
         */
        public void pauseAll() {
            if (player != null) {
                SoundManager.this.pause(player);
            }
            if (players != null) {
                int playersCount = players.size();
                for (int i = 0; i < playersCount; i++) {
                    MediaPlayer player = players.get(i);
                    SoundManager.this.pause(player);
                }
            }
        }

        /**
         * Resume todos los reproductores de este set
         */
        public void resumeAll() {
            if (player != null) {
                SoundManager.this.resume(player);
            }
            if (players != null) {
                int playersCount = players.size();
                for (int i = 0; i < playersCount; i++) {
                    MediaPlayer player = players.get(i);
                    SoundManager.this.resume(player);
                }
            }
        }

        /**
         * Para todos los reproductores de este set
         */
        public void stopAll() {
            if (player != null) {
                SoundManager.this.stop(player);
            }
            if (players != null) {
                int playersCount = players.size();
                for (int i = 0; i < playersCount; i++) {
                    MediaPlayer player = players.get(i);
                    SoundManager.this.stop(player);
                }
            }
        }

        /**
         * Disposea todos los reproductores de este set
         */
        public void releaseAll() {
            if (player != null) {
                player.release();
                player = null;
            }
            if (players != null) {
                int playersCount = players.size();
                for (int i = 0; i < playersCount; i++) {
                    MediaPlayer player = players.get(i);
                    player.release();
                    player = null;
                }
            }
        }

    }
}
