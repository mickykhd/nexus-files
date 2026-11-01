#!/bin/bash
# Seeds realistic test data onto the emulator so Nexus Files has real content:
# folders, media, duplicates, large files, stale APKs, old screenshots and junk.
set -e
ADB=/home/ashrumochan/Android/Sdk/platform-tools/adb
S=/storage/emulated/0

$ADB shell "mkdir -p $S/Documents $S/Download/Work $S/DCIM/Camera $S/Movies/Clips $S/Pictures/Screenshots $S/Pictures/Memes $S/Download/Installs"

echo "== documents =="
$ADB shell "dd if=/dev/zero of=$S/Documents/quarterly_report.pdf bs=1m count=4 2>/dev/null"
$ADB shell "dd if=/dev/zero of=$S/Documents/presentation_deck.pptx bs=1m count=9 2>/dev/null"
$ADB shell "dd if=/dev/zero of=$S/Documents/tax_return_confidential.pdf bs=1m count=4 2>/dev/null"
$ADB shell "dd if=/dev/urandom of=$S/Documents/wallet_recovery_seed.pem bs=1k count=4 2>/dev/null"
$ADB shell "echo 'private key material' > $S/Documents/sec_notes.txt"

echo "== work / code =="
$ADB shell "dd if=/dev/zero of=$S/Download/Work/project_mockup.fig bs=1m count=19 2>/dev/null"
$ADB shell "dd if=/dev/zero of=$S/Download/nexus_kernel_v4.zip bs=1m count=142 2>/dev/null"

echo "== duplicates (42MB x3) =="
$ADB shell "dd if=/dev/zero of=$S/DCIM/Camera/IMG_2024.DNG bs=1m count=42 2>/dev/null"
$ADB shell "cp $S/DCIM/Camera/IMG_2024.DNG $S/Download/IMG_2024.DNG"
$ADB shell "cp $S/DCIM/Camera/IMG_2024.DNG $S/Pictures/IMG_2024.DNG"

echo "== large unused files =="
$ADB shell "dd if=/dev/zero of=$S/Movies/Clips/4K_Gimbal_Drone_Export.mp4 bs=1m count=560 2>/dev/null"
$ADB shell "dd if=/dev/zero of=$S/Download/Android_Studio_Install.img bs=1m count=620 2>/dev/null"
$ADB shell "dd if=/dev/zero of=$S/DCIM/ScreenRecord_GrandTheft.mp4 bs=1m count=520 2>/dev/null"

echo "== stale apks =="
for i in 1 2 3; do
  $ADB shell "dd if=/dev/zero of=$S/Download/Installs/app-release_v$i.apk bs=1m count=22 2>/dev/null"
done

echo "== old screenshots + memes =="
for i in $(seq 1 12); do
  $ADB shell "dd if=/dev/zero of=$S/Pictures/Screenshots/Screenshot_20240$i.png bs=1m count=2 2>/dev/null"
  $ADB shell "dd if=/dev/zero of=$S/Pictures/Memes/meme_pack_$i.jpg bs=1m count=3 2>/dev/null"
done
# Backdate them ~8 months
$ADB shell "find $S/Pictures/Screenshots $S/Pictures/Memes -type f -exec touch -d '2026-02-10' {} +"

echo "== junk / cache =="
$ADB shell "mkdir -p $S/DCIM/.thumbnails $S/Download/cache"
$ADB shell "dd if=/dev/zero of=$S/DCIM/.thumbnails/thumbdata4-1 db bs=1m count=34 2>/dev/null" || true
$ADB shell "dd if=/dev/zero of=$S/DCIM/.thumbnails/junk1 bs=1m count=34 2>/dev/null"
$ADB shell "dd if=/dev/zero of=$S/Download/cache/webview_cache.tmp bs=1m count=20 2>/dev/null"
$ADB shell "dd if=/dev/zero of=$S/Download/failed_download.crdownload bs=1m count=12 2>/dev/null"
$ADB shell "echo debug > $S/Download/install.log"

echo "== a few recent media =="
$ADB shell "dd if=/dev/zero of=$S/DCIM/Camera/VID_20261028.mp4 bs=1m count=24 2>/dev/null"
$ADB shell "dd if=/dev/zero of=$S/DCIM/Camera/IMG_20261030.jpg bs=1m count=5 2>/dev/null"
$ADB shell "dd if=/dev/zero of=$S/Music/ambient_mix.flac bs=1m count=11 2>/dev/null"

echo "== done =="
$ADB shell "du -sh $S 2>/dev/null | tail -1"
