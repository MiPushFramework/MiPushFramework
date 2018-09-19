function copy_files {
  # /data/misc/riru/modules/template exists -> libriru_template.so will be loaded
  # Change "template" to your module name
  # You can also use this folder as your config folder
  NAME="riru_mipushfake"
  mkdir -p $TMP_DIR_MAGISK/data/misc/riru/modules/$NAME
  cp $MODULE_NAME/template_override/riru_module.prop $TMP_DIR_MAGISK/data/misc/riru/modules/$NAME/module.prop

  cp $MODULE_NAME/template_override/config.sh $TMP_DIR_MAGISK
  cp $MODULE_NAME/template_override/module.prop $TMP_DIR_MAGISK
  
  echo "310030" > $TMP_DIR_MAGISK/data/misc/riru/modules/$NAME/gsm.sim.operator.numeric
  echo "us" > $TMP_DIR_MAGISK/data/misc/riru/modules/$NAME/gsm.sim.operator.iso-country
  mkdir $TMP_DIR_MAGISK/data/misc/riru/modules/$NAME/packages
  touch $TMP_DIR_MAGISK/data/misc/riru/modules/$NAME/packages/com.google.android.gms
  touch $TMP_DIR_MAGISK/data/misc/riru/modules/$NAME/packages/com.google.android.gsf
  touch $TMP_DIR_MAGISK/data/misc/riru/modules/$NAME/packages/com.google.android.apps.map
}