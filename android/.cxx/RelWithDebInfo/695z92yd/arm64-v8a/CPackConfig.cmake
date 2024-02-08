# This file will be configured to contain variables for CPack. These variables
# should be set in the CMake list file of the project before CPack module is
# included. The list of available CPACK_xxx variables and their associated
# documentation may be obtained using
#  cpack --help-variable-list
#
# Some variables are common to all generators (e.g. CPACK_PACKAGE_NAME)
# and some are specific to a generator
# (e.g. CPACK_NSIS_EXTRA_INSTALL_COMMANDS). The generator specific variables
# usually begin with CPACK_<GENNAME>_xxxx.


set(CPACK_BUILD_SOURCE_DIRS "C:/Clone Project/r_barcode/r_barcode/android/src/main/cpp;C:/Clone Project/r_barcode/r_barcode/android/.cxx/RelWithDebInfo/695z92yd/arm64-v8a")
set(CPACK_CMAKE_GENERATOR "Ninja")
set(CPACK_COMPONENT_UNSPECIFIED_HIDDEN "TRUE")
set(CPACK_COMPONENT_UNSPECIFIED_REQUIRED "TRUE")
set(CPACK_DEBIAN_PACKAGE_MAINTAINER "Frank Barchard <fbarchard@chromium.org>")
set(CPACK_DEBIAN_PACKAGE_PRIORITY "optional")
set(CPACK_DEBIAN_PACKAGE_SECTION "other")
set(CPACK_DEFAULT_PACKAGE_DESCRIPTION_FILE "C:/Users/pushparaj.manickam/AppData/Local/Android/Sdk/cmake/3.18.1/share/cmake-3.18/Templates/CPack.GenericDescription.txt")
set(CPACK_DEFAULT_PACKAGE_DESCRIPTION_SUMMARY "Project built using CMake")
set(CPACK_GENERATOR "DEB;RPM")
set(CPACK_INSTALL_CMAKE_PROJECTS "C:/Clone Project/r_barcode/r_barcode/android/.cxx/RelWithDebInfo/695z92yd/arm64-v8a;Project;ALL;/")
set(CPACK_INSTALL_PREFIX "C:/Program Files (x86)/Project")
set(CPACK_MODULE_PATH "")
set(CPACK_NSIS_DISPLAY_NAME "libyuv 0.0.")
set(CPACK_NSIS_INSTALLER_ICON_CODE "")
set(CPACK_NSIS_INSTALLER_MUI_ICON_CODE "")
set(CPACK_NSIS_INSTALL_ROOT "$PROGRAMFILES")
set(CPACK_NSIS_PACKAGE_NAME "libyuv 0.0.")
set(CPACK_NSIS_UNINSTALL_NAME "Uninstall")
set(CPACK_OUTPUT_CONFIG_FILE "C:/Clone Project/r_barcode/r_barcode/android/.cxx/RelWithDebInfo/695z92yd/arm64-v8a/CPackConfig.cmake")
set(CPACK_PACKAGE_CONTACT "fbarchard@chromium.org")
set(CPACK_PACKAGE_DEFAULT_LOCATION "/")
set(CPACK_PACKAGE_DESCRIPTION "YUV library and YUV conversion tool")
set(CPACK_PACKAGE_DESCRIPTION_FILE "C:/Users/pushparaj.manickam/AppData/Local/Android/Sdk/cmake/3.18.1/share/cmake-3.18/Templates/CPack.GenericDescription.txt")
set(CPACK_PACKAGE_DESCRIPTION_SUMMARY "YUV library")
set(CPACK_PACKAGE_FILE_NAME "libyuv-0.0.-linux-amd-64")
set(CPACK_PACKAGE_INSTALL_DIRECTORY "libyuv 0.0.")
set(CPACK_PACKAGE_INSTALL_REGISTRY_KEY "libyuv 0.0.")
set(CPACK_PACKAGE_NAME "libyuv")
set(CPACK_PACKAGE_RELOCATABLE "true")
set(CPACK_PACKAGE_VENDOR "Frank Barchard")
set(CPACK_PACKAGE_VERSION "0.0.")
set(CPACK_PACKAGE_VERSION_MAJOR "0")
set(CPACK_PACKAGE_VERSION_MINOR "0")
set(CPACK_PACKAGE_VERSION_PATCH "1")
set(CPACK_RESOURCE_FILE_LICENSE "C:/Clone Project/r_barcode/r_barcode/android/src/main/cpp/libyuv/LICENSE")
set(CPACK_RESOURCE_FILE_README "C:/Users/pushparaj.manickam/AppData/Local/Android/Sdk/cmake/3.18.1/share/cmake-3.18/Templates/CPack.GenericDescription.txt")
set(CPACK_RESOURCE_FILE_WELCOME "C:/Users/pushparaj.manickam/AppData/Local/Android/Sdk/cmake/3.18.1/share/cmake-3.18/Templates/CPack.GenericWelcome.txt")
set(CPACK_SET_DESTDIR "OFF")
set(CPACK_SOURCE_GENERATOR "TBZ2;TGZ;TXZ;TZ")
set(CPACK_SOURCE_OUTPUT_CONFIG_FILE "C:/Clone Project/r_barcode/r_barcode/android/.cxx/RelWithDebInfo/695z92yd/arm64-v8a/CPackSourceConfig.cmake")
set(CPACK_SOURCE_RPM "OFF")
set(CPACK_SOURCE_TBZ2 "ON")
set(CPACK_SOURCE_TGZ "ON")
set(CPACK_SOURCE_TXZ "ON")
set(CPACK_SOURCE_TZ "ON")
set(CPACK_SOURCE_ZIP "OFF")
set(CPACK_SYSTEM_NAME "linux-amd-64")
set(CPACK_TOPLEVEL_TAG "linux-amd-64")
set(CPACK_WIX_SIZEOF_VOID_P "8")

if(NOT CPACK_PROPERTIES_FILE)
  set(CPACK_PROPERTIES_FILE "C:/Clone Project/r_barcode/r_barcode/android/.cxx/RelWithDebInfo/695z92yd/arm64-v8a/CPackProperties.cmake")
endif()

if(EXISTS ${CPACK_PROPERTIES_FILE})
  include(${CPACK_PROPERTIES_FILE})
endif()
