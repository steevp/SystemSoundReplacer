#!/system/bin/sh
set -e

print_usage() {
  cat <<EOF
Usage:
  $(basename $0) replace <sound file> <replacement>
  $(basename $0) restore <sound file>
EOF
}

make_backup() {
  [ $# -eq 1 ] || exit 1
  if [ -r "$1" -a ! -r "${1}.bak" ]; then
    echo "Backing up $1"
    cat "$1" > "${1}.bak"
  else
    echo "Already backed up!"
  fi
}

replace_file() {
  [ $# -eq 2 ] || exit 1
  if [ -r "$1" -a -r "$2" ]; then
    echo "Replacing $2 with $1"
    cat "$2" > "$1"
    rm "$2"
  else
    echo "Either $1 or $2 doesn't exist"
    exit 1
  fi
}

remount() {
  [ $# -eq 1 ] || exit 1
  FSTYPE="$(/system/bin/mount | grep ' /system ' | tr -s ' ' | cut -d' ' -f3)"
  if [ -z "$FSTYPE" ]; then
    echo "Unable to detect /system FS type!"
    exit 1
  fi
  echo "Remounting /system $1"
  /system/bin/mount -o remount,"$1" -t "$FSTYPE" /system
}

case "$1" in
  replace)
    remount rw
    make_backup "$2"
    replace_file "$2" "$3"
    remount ro
  ;;
  restore)
    remount rw
    replace_file "$2" "${2}.bak"
    remount ro
  ;;
  *)
    print_usage
    exit 1
  ;;
esac
