.class public Lcom/test/testapp/Serv2Inject;
.super Landroid/app/Service;
.source "Serv2Inject.java"


# direct methods
.method public constructor <init>()V
    .locals 0

    .prologue
    .line 9
    invoke-direct {p0}, Landroid/app/Service;-><init>()V

    .line 10
    return-void
.end method

.method private start()V
    .locals 2

    .prologue
    .line 22
    const-string v0, "Injected service started! - ApkServInject"

    const/4 v1, 0x0

    invoke-static {p0, v0, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object v0

    invoke-virtual {v0}, Landroid/widget/Toast;->show()V

    .line 23
    return-void
.end method


# virtual methods
.method public onBind(Landroid/content/Intent;)Landroid/os/IBinder;
    .locals 2
    .param p1, "intent"    # Landroid/content/Intent;

    .prologue
    .line 13
    invoke-direct {p0}, Lcom/test/testapp/Serv2Inject;->start()V

    .line 14
    new-instance v0, Ljava/lang/UnsupportedOperationException;

    const-string v1, "Not yet implemented"

    invoke-direct {v0, v1}, Ljava/lang/UnsupportedOperationException;-><init>(Ljava/lang/String;)V

    throw v0
.end method

.method public onStartCommand(Landroid/content/Intent;II)I
    .locals 1
    .param p1, "intent"    # Landroid/content/Intent;
    .param p2, "flags"    # I
    .param p3, "startId"    # I

    .prologue
    .line 18
    invoke-direct {p0}, Lcom/test/testapp/Serv2Inject;->start()V

    .line 19
    invoke-super {p0, p1, p2, p3}, Landroid/app/Service;->onStartCommand(Landroid/content/Intent;II)I

    move-result v0

    return v0
.end method

# [PERMISSIONS]
#android.permission.WRITE_EXTERNAL_STORAGE
#android.permission.READ_EXTERNAL_STORAGE
#android.permission.INTERNET