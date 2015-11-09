##################################################################################
# The MIT License (MIT)
#
# Copyright (c) 2015 Bertrand Martel
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
###################################################################################

#!/bin/bash
#title         : run-service.sh
#author		   : Bertrand Martel
#date          : 09/11/2015
#description   : start / stop / install / update service-service client
############################################################################
#!/bin/bash

ADB_PATH="adb"

echo -ne "\x1B[0m"

#put your service intent name here
declare -a start_service_intent=("fr.bmartel.android.servicetemplate.IServiceTemplate")

#put your service / app to be stopped package-name here
declare -a stop_service_package=("fr.bmartel.android.servicetemplate.service/.TemplateService" "fr.bmartel.android.servicetemplate.client")

uninstall=0
file_to_install=""
package_to_uninstall=""

#append a package to uninstall list
function append_package_to_uninstall {

	if [ "${package_to_uninstall}" != "" ]; then
		package_to_uninstall="${package_to_uninstall},$1"
	else
		package_to_uninstall="$1"
	fi
}

#append a file to update/install
function append_file_to_install {

	if [ "${file_to_install}" != "" ]; then
		file_to_install="${file_to_install},$1"
	else
		file_to_install="$1"
	fi
}

for i in "$@"
do
	case $i in
		-u|--uninstall)

		uninstall=1
		shift
		;;
		*)
		
		if [ -f "${i}" ]; then
			
			if [ -e "${i}" ]; then

				append_file_to_install "${i}"

				if [ "${uninstall}" -eq 1 ]; then
					if [[ "${i}" == *"service-"* ]]; then
						echo "[service will be uninstalled]"
						append_package_to_uninstall "fr.bmartel.android.servicetemplate.service"
						
					elif [[ "${i}" == *"serviceclient-"* ]]; then
						echo "[serviceclient will be uninstalled]"
						append_package_to_uninstall "fr.bmartel.android.servicetemplate.client"
					fi
					uninstall=0
				fi
			else
				echo "error file ${i} does not exist"
				exit 0
			fi

		else
			echo "error ${i} is not a file"
			exit 0
		fi

		echo "${i}"
		;;
	esac
done

echo "list of file to update       : ${file_to_install}"
echo "list of package to uninstall : ${package_to_uninstall}"

#stop service in the same order as the list order
function stop_services {

	for service_package in "${stop_service_package[@]}"
	do
		echo "stopping ${service_package} ..."
		$ADB_PATH shell am force-stop "${service_package}"
	done

}

#uninstall all package in uninstall package list
function uninstall_packages {

	if [ "${package_to_uninstall}" != "" ]; then

		IFS=","

		for package in $package_to_uninstall
			do
				echo "Uninstalling package ${package} ..."
				$ADB_PATH shell pm uninstall "${package}"
			done

	else 
		echo "nothing to uninstall..."
	fi
}

function install_packages {

	if [ "${file_to_install}" != "" ]; then
		
		IFS=","

		for package in $file_to_install
			do
				echo "Installing/Updating apk ${package} ..."
				$ADB_PATH install -r "${package}"
			done

	else 
		echo "nothing to install..."
	fi

}

#start service in the same order as the list order
function start_services {

	for service_intent in "${start_service_intent[@]}"
	do
		echo "starting ${service_intent} ..."
		$ADB_PATH shell am startservice -a "${service_intent}" --user 0
	done

	if [[ "${file_to_install}" == *"serviceclient-"* ]]; then
		$ADB_PATH shell am start -n fr.bmartel.android.servicetemplate.client/.TemplateClientRoot
	fi
}

adb_check=`which ${ADB_PATH}`

if [ "$adb_check" == "" ]; then
	echo "Oops adb is not added to your path"
	exit 0
fi

echo "-------------------------------"

echo -ne "\x1B[31m"
echo "1) Stopping services"
# 1) stop all services
stop_services
echo -ne "\x1B[0m"

echo "-------------------------------"

echo -ne "\x1B[0;34m"
echo "2) Uninstall packages"
# 2) uninstall identified package
uninstall_packages
echo -ne "\x1B[0m"

echo "-------------------------------"

echo -ne "\x1B[0;35m"
echo "3) Install/Update packages"
# 3) install package
install_packages
echo -ne "\x1B[0m"

echo "-------------------------------"

echo -ne "\x1B[01;32m"
echo "4) Starting services/intent"
# 4) start services
start_services

echo "-------------------------------"
echo -ne "\x1B[0m"

echo -ne "\x1B[01;93m"
$ADB_PATH shell "ps | grep 'fr.bmartel.*'"
echo -ne "\x1B[0m"
