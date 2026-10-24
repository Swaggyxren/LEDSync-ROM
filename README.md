# LEDSync-ROM

Root-free LED control app for the Tecno Pova 5 Pro (LH8n), designed to ship inside custom ROMs (LineageOS, AOSP) as an in-tree privileged system app (`com.xiannn.ledsync.system`).

Built with Jetpack Compose, Material 3 Expressive UI, and direct Linux sysfs integration.

---

## Architecture & Features

1. **Direct Root-Free Sysfs I/O**:
   - Accesses `/sys/led/led/tran_led_cmd` directly via standard Java I/O without `su -c` overhead.
   - Initialized via SELinux (`rom/sepolicy/`) and init rules (`rom/init/init.ledsync.rc`).

2. **Stock ROM Behavior Parity**:
   - **Canonical Presets**: Soft, Racing, Dreamy, Breath, Party, Pure, Starburst, Halo, Lightning, Sunrise.
   - **Dynamic Volume Follower**: Tracks `android.media.VOLUME_CHANGED_ACTION` and syncs hardware gain (`00 21 [GAIN] 00 00 00`) live across volume levels 1 to 15.
   - **Flip-to-Mute (Sensor Integration)**: Uses the Accelerometer Z-axis ($G_z < 0$) to detect face-down placement on a flat surface and mute notification/call lights.
   - **Rear Camera Suppression**: Automatically shuts off the rear LEDs whenever the rear camera is active to prevent lens flare in photos and videos.
   - **DND & Battery Saver**: Respects ZenMode and PowerSaveMode to avoid sleep disturbance and battery drain.
   - **Charging Tiers**: 3-tier battery pulse on plug-in (<20%, 20–60%, 60–100%, 100% full) matching stock Transsion Lights Service.

3. **Material 3 Expressive UI**:
   - Complete 1:1 implementation of the Figma design:
     - **Home**: Hero device overview card with SVG rear LED strip, master power toggle, battery & sysfs status tiles, audio/app/battery quick launchers, CPU/RAM load overview.
     - **Hardware Lab**: Live sysfs console log viewer (`led-console ~ sysfs`), preset triggers, and manual testing.
     - **Tweaks**: Sensor toggles (Flip-to-Mute, Camera suppression, DND, Battery Saver), Theme selector (System / Light / Dark).
     - **Performance Monitor**: Realtime CPU load graph and RAM breakdown (Used, Free, Total).
     - **App LED Sync**: Search installed applications, system app filter, per-app custom LED pattern selector.
     - **Battery LED Studio**: 150px circular battery indicator, trigger threshold sliders (Low, Critical, Full) with pattern selection.
     - **Audio Reactive LED**: Animated equalizer spectrum, Dynamic RGB (Party) vs Static White (Breath) toggle, live volume follower switch, reactivity slider.

---

## ROM Integration (AOSP / LineageOS)

Include this directory in your device tree or local manifest:

```make
# In device/transsion/LH8n/device.mk:
PRODUCT_PACKAGES += \
    LEDSync-ROM \
    privapp-permissions-ledsync.xml \
    init.ledsync.rc
```

The app will compile as a privileged system application in `/system_ext/priv-app/LEDSync-ROM/` with platform signature permissions.
