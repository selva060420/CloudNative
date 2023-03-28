export KUBECONFIG="/local/euimkks/.kube/config"
export NAMESPACE=dev
export co=" --dry-run=client -o yaml "
export so=" --dry-run=server -o yaml "
export now=" --force --grace-period=0 "
alias ks='kubectl '
alias global='cs,no,pv,csr,crd,psp,pc,sc,clusterrole,clusterrolebinding '
alias main='cm,pvc,po,svc,sa,ds,deploy,sts,cj,job,secrets,role,rolebinding '
alias other='limits,quota,hpa,rs,rc,sa,ep '
alias aa='role,rolebinding,clusterrole,clusterrolebinding '
alias network='ing,netpol,proxy,extensionservice '
alias everything='cs,no,pv,csr,crd,psp,pc,sc,cm,pvc,po,svc,ds,deploy,sts,cj,job,secrets,limits,quota,hpa,rs,rc,sa,ep,ing,netpol,proxy,extensionservice,role,rolebinding,clusterrole,clusterrolebinding '
alias g='get '
alias des='describe '
alias log='logs '
alias exec='exec -it '
alias newns='create namespace '
alias delns='delete namespace '
alias del='delete '
alias force=' --all --grace-period=0 --force '
alias ns='--namespace $NAMESPACE '
alias kset='kubectl config set-context --current --namespace '
[ -f ~/.fzf.bash ] && source ~/.fzf.bash
