KUBE_DIR="$(dirname "$KUBECONFIG")"
CERT_DIR=$KUBE_DIR/certificates
INSTALL_DIR=$HOME/temp-installers
CURRENT_DIR=$PWD

update_vim_settings(){
mkdir -p ~/.vim ~/.vim/autoload ~/.vim/backup ~/.vim/colors ~/.vim/plugged
touch ~/.vimrc
curl -fLo ~/.vim/autoload/plug.vim --create-dirs https://raw.githubusercontent.com/junegunn/vim-plug/master/plug.vim
curl -fLo ~/.vim/colors/molokai.vim --create-dirs https://raw.githubusercontent.com/tomasr/molokai/master/colors/molokai.vim

cat << EOF > ~/.vimrc
set nocompatible

set cursorline

set shiftwidth=2

set tabstop=2

set expandtab

set nobackup

set scrolloff=10

set nowrap

set incsearch

set ignorecase

set smartcase

set showcmd

set showmode

set showmatch

set hlsearch

set history=1000

set wildmenu

set wildmode=list:longest

set wildignore=*.docx,*.jpg,*.png,*.gif,*.pdf,*.pyc,*.exe,*.flv,*.img,*.xlsx

set statusline=

set statusline+=\ %F\ %M\ %Y\ %R

set statusline+=%=

set statusline+=\ ascii:\ %b\ hex:\ 0x%B\ row:\ %l\ col:\ %c\ percent:\ %p%%

set laststatus=2

EOF
}
check_kubeconfig(){
if test -f "$KUBECONFIG"; 
then
	make_or_remove_dir $CERT_DIR
	KUBE_API=$(grep server $KUBECONFIG  | awk '{ print $2 }' | head -n1 )
	grep client-certificate-data $KUBECONFIG  | awk '{ print $2 }' | base64 -d > $CERT_DIR/cert.pem
	grep client-key-data $KUBECONFIG  | awk '{ print $2 }' | base64 -d > $CERT_DIR/key.pem
	grep certificate-authority-data $KUBECONFIG  | awk '{ print $2 }' | base64 -d > $CERT_DIR/cacert.pem
	if test -f $CERT_DIR/cert.pem && test -f $CERT_DIR/key.pem && test -f $CERT_DIR/cacert.pem ; then
		echo -e "curl $KUBE_API/version --cacert $CERT_DIR/cacert.pem --key $CERT_DIR/key.pem --cert $CERT_DIR/cert.pem"
	fi
else
	echo -e "\nError: Check if \033[4mKUBECONFIG\033[0m is configured in ~/.bashrc. Do printenv | grep KUBECONFIG"
fi
}
make_or_remove_dir(){
if [ -d $1 ]
then
    rm -rf $1    
fi
mkdir -p $1
}

dialog_box_for_k8s(){
#!/bin/bash
HEIGHT=15
WIDTH=40
CHOICE_HEIGHT=4
BACKTITLE="Minikube or Kind installation"
TITLE="Local Kubernetes Cluster Installation"
MENU="Choose one of the following:"

if ! [ -x "$(command -v dialog)" ]; then
  echo -e "\n--------------------------------------------------------------\n"
  echo -e "\nError: dialog is not installed. Hence installing\n"
  sudo apt install dialog
fi

OPTIONS=(1 "Minikube"
         2 "Kind")

CHOICE=$(dialog --clear \
                --backtitle "$BACKTITLE" \
                --title "$TITLE" \
                --menu "$MENU" \
                --keep-tite \
                $HEIGHT $WIDTH $CHOICE_HEIGHT \
                "${OPTIONS[@]}" \
                2>&1 >/dev/tty)

case $CHOICE in
        1)
            echo -e "\n--------------------------------------------------------------\n"
            if ! [ -x "$(command -v minikube)" ]; then
              echo -e "\n--------------------------------------------------------------\n"
              echo -e "\nError: minikube is not installed. Hence installing\n"
              curl -LO "https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64"
              sudo install minikube-linux-amd64 /usr/local/bin/minikube
            else
              echo -e "\nError: \033[4mMinikube\033[0m already installed!!\n"
            fi
            ;;
        2)
            echo -e "\n--------------------------------------------------------------\n"
            if ! [ -x "$(command -v kind)" ]; then
              echo -e "\n--------------------------------------------------------------\n"
              echo -e "\nError: kind is not installed. Hence installing\n"
              curl -Lo ./kind "https://kind.sigs.k8s.io/dl/v0.14.0/kind-linux-amd64"
              chmod +x ./kind
              sudo install ./kind /usr/local/bin/kind
            else
              echo -e "\nError: \033[4mKind\033[0m already installed!!\n"
            fi
            ;;
esac
}
check_if_installed(){

update_vim_settings
make_or_remove_dir $INSTALL_DIR
cd $INSTALL_DIR
# Check required commands are available and executable in $PATH

if ! [ -x "$(command -v xclip)" ]; then
  echo -e "\n--------------------------------------------------------------\n"
  echo -e "\nError: xclip is not installed. Hence installing\n"
  sudo apt update
  sudo apt install xclip
fi

if ! [ -x "$(command -v docker)" ]; then
  echo -e "\n--------------------------------------------------------------\n"
  echo -e "\nError: docker is not installed. Hence installing\n"
  sudo apt update
  sudo apt install -y apt-transport-https ca-certificates curl software-properties-common
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
  sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu bionic stable"
  sudo apt update
  sudo apt install -y docker-ce
  sudo usermod -aG docker $USER
  sudo chmod 666 /var/run/docker.sock
fi

if ! [ -x "$(command -v crictl)" ]; then
  echo -e "\n--------------------------------------------------------------\n"
  echo -e "\nError: crictl is not installed. Hence installing\n"
  VERSION="v1.24.1"
  curl -L https://github.com/kubernetes-sigs/cri-tools/releases/download/$VERSION/crictl-${VERSION}-linux-amd64.tar.gz --output crictl-${VERSION}-linux-amd64.tar.gz
  sudo tar zxvf crictl-$VERSION-linux-amd64.tar.gz -C /usr/local/bin
  rm -f crictl-$VERSION-linux-amd64.tar.gz
fi

if ! [ -x "$(command -v kubectl)" ]; then
  echo -e "\n--------------------------------------------------------------\n"
  echo -e "\nError: kubectl is not installed. Hence installing\n"
  curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
  sudo install kubectl /usr/local/bin/kubectl
fi

if ! [ -x "$(command -v helm)" ]; then
  echo -e "\n--------------------------------------------------------------\n"
  echo -e "\nError: helm is not installed. Hence installing\n"
  curl -Lo helm.tgz "https://get.helm.sh/helm-v3.10.3-linux-amd64.tar.gz"
  tar -xf helm.tgz
  sudo install linux-amd64/helm /usr/local/bin/helm
fi

dialog_box_for_k8s
check_kubeconfig

if [ -x "$(command -v git)" ]; then
  if ! test -f "$HOME/.krew/bin/kubectl-krew"; then
    echo -e "\n--------------------------------------------------------------\n"
    echo -e "\nError: krew is not installed. Hence installing\n"
    OS="$(uname | tr '[:upper:]' '[:lower:]')"
    ARCH="$(uname -m | sed -e 's/x86_64/amd64/' -e 's/\(arm\)\(64\)\?.*/\1\2/' -e 's/aarch64$/arm64/')"
    KREW="krew-${OS}_${ARCH}"
    curl -fsSLO "https://github.com/kubernetes-sigs/krew/releases/latest/download/${KREW}.tar.gz"
    tar zxvf "${KREW}.tar.gz"
    ./"${KREW}" install krew
  fi
else
  echo -e "\n--------------------------------------------------------------\n"
  echo -e "\nError: krew is not installed. Please install \033[4mGit\033[0m first!!!\n"
fi
KREW_FILE="$HOME/.krew/bin/"
if test -f "$HOME/.krew/bin/kubectl-krew"; then
  if ! test -f "$HOME/.krew/bin/kubectl-cert_manager"; then
    echo -e "\n--------------------------------------------------------------\n"
    echo -e "\nError: cert-manager plugin is not installed. Hence installing\n"
    $KREW_FILE/kubectl-krew install cert-manager
  fi
  if ! test -f "$HOME/.krew/bin/kubectl-graph"; then
    echo -e "\n--------------------------------------------------------------\n"
    echo -e "\nError: graph plugin is not installed. Hence installing\n"
    $KREW_FILE/kubectl-krew install graph
  fi
  if ! test -f "$HOME/.krew/bin/kubectl-df_pv"; then
    echo -e "\n--------------------------------------------------------------\n"
    echo -e "\nError: df-pv plugin is not installed. Hence installing\n"
    $KREW_FILE/kubectl-krew install df-pv
  fi
  if ! test -f "$HOME/.krew/bin/kubectl-oomd"; then
    echo -e "\n--------------------------------------------------------------\n"
    echo -e "\nError: oomd plugin is not installed. Hence installing\n"
    $KREW_FILE/kubectl-krew install oomd
  fi
  if ! test -f "$HOME/.krew/bin/kubectl-cost"; then
    echo -e "\n--------------------------------------------------------------\n"
    echo -e "\nError: cost plugin is not installed. Hence installing\n"
    $KREW_FILE/kubectl-krew install cost
  fi
  if ! test -f "$HOME/.krew/bin/kubectl-pod_dive"; then
    echo -e "\n--------------------------------------------------------------\n"
    echo -e "\nError: pod-dive plugin is not installed. Hence installing\n"
    $KREW_FILE/kubectl-krew install pod-dive
  fi
  if ! test -f "$HOME/.krew/bin/kubectl-pod_lens"; then
    echo -e "\n--------------------------------------------------------------\n"
    echo -e "\nError: pod-lens plugin is not installed. Hence installing\n"
    $KREW_FILE/kubectl-krew install pod-lens
  fi
  if ! test -f "$HOME/.krew/bin/kubectl-rbac_lookup"; then
    echo -e "\n--------------------------------------------------------------\n"
    echo -e "\nError: rbac-lookup plugin is not installed. Hence installing\n"
    $KREW_FILE/kubectl-krew install rbac-lookup
  fi
  if ! test -f "$HOME/.krew/bin/kubectl-rbac_tool"; then
    echo -e "\n--------------------------------------------------------------\n"
    echo -e "\nError: rbac-tool plugin is not installed. Hence installing\n"
    $KREW_FILE/kubectl-krew install rbac-tool
  fi
  if ! test -f "$HOME/.krew/bin/kubectl-ttsum"; then
    echo -e "\n--------------------------------------------------------------\n"
    echo -e "\nError: ttsum plugin is not installed. Hence installing\n"
    $KREW_FILE/kubectl-krew install ttsum
  fi
  if ! test -f "$HOME/.krew/bin/kubectl-view_cert"; then
    echo -e "\n--------------------------------------------------------------\n"
    echo -e "\nError: view-cert plugin is not installed. Hence installing\n"
    $KREW_FILE/kubectl-krew install view-cert
  fi
  if ! test -f "$HOME/.krew/bin/kubectl-view_secret"; then
    echo -e "\n--------------------------------------------------------------\n"
    echo -e "\nError: view-secret plugin is not installed. Hence installing\n"
    $KREW_FILE/kubectl-krew install view-secret
  fi
fi
ans=$($KREW_FILE/kubectl-krew list | awk '{print $1}' | wc -l)
if [ $ans == 13 ] ; then
  echo -e "\n\033[4m13\033[0m krew plugins are already installed. Please do '\033[4mkubectl krew list\033[0m' to check\n"
fi

if ! [ -x "$(command -v k9s)" ]; then
  echo -e "\n--------------------------------------------------------------\n"
  echo -e "\n Error: k9s is not installed. Hence installing\n"
  curl -Lo k9s.tgz https://github.com/derailed/k9s/releases/download/v0.26.3/k9s_Linux_x86_64.tar.gz
  tar -xf k9s.tgz
  sudo install k9s /usr/local/bin/
fi

if ! [ -x "$(command -v kubectx)" ]; then
  echo -e "\n--------------------------------------------------------------\n"
  echo -e "\n Error: kubectx is not installed. Hence installing\n"
  curl -Lo kubectx.tgz  https://github.com/ahmetb/kubectx/releases/download/v0.9.0/kubectx_v0.9.0_linux_x86_64.tar.gz
  tar -xf kubectx.tgz
  sudo install kubectx /usr/local/bin/
fi

if ! [ -x "$(command -v kubens)" ]; then
  echo -e "\n--------------------------------------------------------------\n"
  echo -e "\n Error: kubens is not installed. Hence installing\n"
  curl -Lo kubens.tgz  https://github.com/ahmetb/kubectx/releases/download/v0.9.0/kubens_v0.9.0_linux_x86_64.tar.gz 
  tar -xf kubens.tgz
  sudo install kubens /usr/local/bin/
fi
if ! [ -x "$(command -v fzf)" ]; then
  echo -e "\n--------------------------------------------------------------\n"
  echo -e "\n Error: fzf is not installed. Hence installing\n"
  git clone --depth 1 https://github.com/junegunn/fzf.git ~/.fzf
  ~/.fzf/install
fi
rm -f $INSTALL_DIR/*.tgz
cd $CURRENT_DIR
}
check_if_installed
