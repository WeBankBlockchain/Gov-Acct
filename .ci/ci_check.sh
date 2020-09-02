#!/bin/bash

set -e
LOG_INFO() {
    local content=${1}
    echo -e "\033[32m ${content}\033[0m"
}
check_basic()
{
chmod +x gradlew
# check code format
bash gradlew verifyGoogleJavaFormat
# build
./gradlew clean ecdsa jar
./gradlew clean ecdsa bootJar
./gradlew clean gm jar
./gradlew clean gm bootJar
}

download_build_chain()
{
  tag=$(curl -sS "https://gitee.com/api/v5/repos/FISCO-BCOS/FISCO-BCOS/tags" | grep -oe "\"name\":\"v[2-9]*\.[0-9]*\.[0-9]*\"" | cut -d \" -f 4 | sort -V | tail -n 1)
  LOG_INFO "--- current tag: $tag"
  curl -LO "https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/${tag}/build_chain.sh" && chmod u+x build_chain.sh
}

get_sed_cmd()
{
  local sed_cmd="sed -i"
  if [ "$(uname)" == "Darwin" ];then
        sed_cmd="sed -i .bkp"
  fi
  echo "$sed_cmd"
}

prepare_environment()
{
  cp -rf nodes/127.0.0.1/sdk/* src/test/resources/
  sed_cmd=$(get_sed_cmd)
  local node_type="${1}"
  mkdir -p src/main/java/com/webank/blockchain/acct/gov/contract/
  if [ "${node_type}" == "sm" ];then
    $sed_cmd 's/encryptType=0/encryptType=1/g' src/test/resources/application.properties
    cp -rf src/main/sdk/gm/* src/main/java/com/webank/blockchain/acct/gov/contract/
  else
    $sed_cmd 's/encryptType=1/encryptType=0/g' src/test/resources/application.properties
    cp -rf src/main/sdk/ecdsa/* src/main/java/com/webank/blockchain/acct/gov/contract/
  fi

}

build_node()
{
  local node_type="${1}"
  if [ "${node_type}" == "sm" ];then
      ./build_chain.sh -l 127.0.0.1:4 -g
  else
      ./build_chain.sh -l 127.0.0.1:4
  fi
  ./nodes/127.0.0.1/fisco-bcos -v
  ./nodes/127.0.0.1/start_all.sh
}

clean_node()
{
  bash nodes/127.0.0.1/stop_all.sh
  rm -rf nodes
}

 # check integration-test for non-gm node
check_standard_node()
{
  build_node
  prepare_environment
  ## run integration test
  bash gradlew clean ecdsa test --info
  ## clean
  clean_node
}

check_sm_node()
{
  build_node "sm"
  prepare_environment "sm"
  ## run integration test
  bash gradlew clean gm test --info
  ## clean
  clean_node
}

LOG_INFO "------ check_basic---------"
check_basic
LOG_INFO "------ download_build_chain---------"
download_build_chain
LOG_INFO "------ check_standard_node---------"
check_standard_node
LOG_INFO "------ check_sm_node---------"
check_sm_node
LOG_INFO "------ check end---------"
