package fr.nihilus.music.playback

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(ExoMusicPlayerTest::class, QueueManagerTest::class)
class PlaybackTestSuite