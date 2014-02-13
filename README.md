ActivityForceNewTask
====================
Apps can launch other apps' activities inside their task. This makes it
impossible to switch back and forth between the two. This module sets the
FLAG_ACTIVITY_NEW_TASK flag when starting a new activity, which 'fixes' that
(note to devs: only startActivity is hooked for obvious reasons.)

Download Module
===============
http://repo.xposed.info/module/com.germainz.activityforcenewtask

XDA Thread
==========
