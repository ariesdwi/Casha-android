import os
import xml.etree.ElementTree as ET
from xml.dom import minidom
import glob

TRANSLATIONS = {
  "profile_action_cancel": {"en": "Cancel", "id": "Batal", "ar": "إلغاء", "de": "Abbrechen", "es": "Cancelar", "fr": "Annuler", "hi": "रद्द करें", "ja": "キャンセル", "ko": "취소", "pt-BR": "Cancelar", "zh-Hans": "取消"},
  "profile_action_clear_all": {"en": "Clear All", "id": "Bersihkan Semua", "ar": "مسح الكل", "de": "Alle löschen", "es": "Limpiar todo", "fr": "Tout effacer", "hi": "सब साफ करें", "ja": "すべてクリア", "ko": "모두 지우기", "pt-BR": "Limpar Tudo", "zh-Hans": "清除全部"},
  "profile_action_delete": {"ar": "حذف", "de": "Löschen", "en": "Delete", "es": "Eliminar", "fr": "Supprimer", "hi": "हटाएं", "id": "Hapus", "ja": "削除", "ko": "삭제", "pt-BR": "Excluir", "zh-Hans": "删除"},
  "profile_action_delete_account": {"en": "Delete Account", "id": "Hapus Akun", "ar": "حذف الحساب", "de": "Konto löschen", "es": "Eliminar Cuenta", "fr": "Supprimer le compte", "hi": "खाता हटाएं", "ja": "アカウントを削除", "ko": "계정 삭제", "pt-BR": "Excluir Conta", "zh-Hans": "删除账户"},
  "profile_action_done": {"en": "Done", "id": "Selesai", "ar": "تم", "de": "Fertig", "es": "Hecho", "fr": "Terminé", "hi": "पूर्ण", "ja": "完了", "ko": "완료", "pt-BR": "Concluído", "zh-Hans": "完成"},
  "profile_action_edit": {"ar": "تعديل", "de": "Bearbeiten", "en": "Edit", "es": "Editar", "fr": "Modifier", "hi": "संपादित करें", "id": "Edit", "ja": "編集", "ko": "편집", "pt-BR": "Editar", "zh-Hans": "编辑"},
  "profile_action_edit_profile": {"en": "Edit Profile", "id": "Ubah Profil", "ar": "تعديل الملف الشخصي", "de": "Profil bearbeiten", "es": "Editar Perfil", "fr": "Modifier le profil", "hi": "प्रोफ़ाइल संपादित करें", "ja": "プロフィールを編集", "ko": "프로필 편집", "pt-BR": "Editar Perfil", "zh-Hans": "编辑个人资料"},
  "profile_action_logout": {"en": "Logout", "id": "Keluar", "ar": "تسجيل الخروج", "de": "Abmelden", "es": "Cerrar sesión", "fr": "Se déconnecter", "hi": "लॉग आउट", "ja": "ログアウト", "ko": "로그아웃", "pt-BR": "Sair", "zh-Hans": "退出登录"},
  "profile_action_mark_all_read": {"en": "Mark All as Read", "id": "Tandai Semua Sudah Dibaca", "ar": "تعليم الكل كمقروء", "de": "Alle als gelesen markieren", "es": "Marcar Todo como Leído", "fr": "Tout marquer compass lu", "hi": "सभी पढ़ा हुआ चिह्नित करें", "ja": "すべて既読にする", "ko": "모두 읽음 표시", "pt-BR": "Marcar Tudo como Lido", "zh-Hans": "全部标为已读"},
  "profile_action_notifications": {"en": "Notifications", "id": "Notifikasi", "ar": "الإشعارات", "de": "Benachrichtigungen", "es": "Notificaciones", "fr": "Notifications", "hi": "सूचनाएं", "ja": "通知", "ko": "알림", "pt-BR": "Notificações", "zh-Hans": "通知"},
  "profile_action_save": {"en": "Save", "id": "Simpan", "ar": "حفظ", "de": "Speichern", "es": "Guardar", "fr": "Enregistrer", "hi": "सहेजें", "ja": "保存", "ko": "저장", "pt-BR": "Salvar", "zh-Hans": "保存"},
  "profile_categories_edit_active": {"ar": "نشط", "de": "Aktiv", "en": "Active", "es": "Activa", "fr": "Actif", "hi": "सक्रिय", "id": "Aktif", "ja": "有効", "ko": "활성", "pt-BR": "Ativa", "zh-Hans": "活跃"},
  "profile_categories_edit_edit_title": {"ar": "تعديل الفئة", "de": "Kategorie bearbeiten", "en": "Edit Category", "es": "Editar Categoría", "fr": "Modifier la catégorie", "hi": "श्रेणी संपादित करें", "id": "Edit Kategori", "ja": "カテゴリを編集", "ko": "카테고리 편집", "pt-BR": "Editar Categoria", "zh-Hans": "编辑类别"},
  "profile_categories_edit_name": {"ar": "اسم الفئة", "de": "Kategoriename", "en": "Category Name", "es": "Nombre de Categoría", "fr": "Nom de la catégorie", "hi": "श्रेणी का नाम", "id": "Nama Kategori", "ja": "カテゴリ名", "ko": "카테고리 이름", "pt-BR": "Nome da Categoria", "zh-Hans": "类别名称"},
  "profile_categories_edit_new_title": {"ar": "فئة جديدة", "de": "Neue Kategorie", "en": "New Category", "es": "Nueva Categoría", "fr": "Nouvelle catégorie", "hi": "नई श्रेणी", "id": "Kategori Baru", "ja": "新しいカテゴリ", "ko": "새 카테고리", "pt-BR": "Nova Categoria", "zh-Hans": "新类别"},
  "profile_categories_empty": {"ar": "لا توجد فئات مخصصة", "de": "Keine benutzerdefinierten Kategorien", "en": "No custom categories", "es": "Sin categorías personalizadas", "fr": "Aucune catégorie personnalisée", "hi": "कोई कस्टम श्रेणियां नहीं", "id": "Tidak ada kategori kustom", "ja": "カスタムカテゴリなし", "ko": "사용자 카테고리 없음", "pt-BR": "Sem categorias personalizadas", "zh-Hans": "无自定义类别"},
  "profile_categories_section_system": {"ar": "فئات النظام", "de": "Systemkategorien", "en": "System Categories", "es": "Categorías del Sistema", "fr": "Catégories système", "hi": "सिस्टम श्रेणियां", "id": "Kategori Sistem", "ja": "システムカテゴリ", "ko": "시스템 카테고리", "pt-BR": "Categorias do Sistema", "zh-Hans": "系统类别"},
  "profile_categories_section_user": {"ar": "فئات المستخدم", "de": "Benutzerkategorien", "en": "User Categories", "es": "Categorías del Usuario", "fr": "Catégories utilisateur", "hi": "उपयोगकर्ता श्रेणियां", "id": "Kategori Pengguna", "ja": "ユーザーカテゴリ", "ko": "사용자 카테고리", "pt-BR": "Categorias do Usuário", "zh-Hans": "用户类别"},
  "profile_categories_status_inactive": {"ar": "غير نشط", "de": "Inaktiv", "en": "Inactive", "es": "Inactiva", "fr": "Inactif", "hi": "निष्क्रिय", "id": "Tidak Aktif", "ja": "無効", "ko": "비활성", "pt-BR": "Inativa", "zh-Hans": "不活跃"},
  "profile_category_active": {"en": "Active", "id": "Aktif", "ar": "نشط", "de": "Aktiv", "es": "Activa", "fr": "Actif", "hi": "सक्रिय", "ja": "有効", "ko": "활성", "pt-BR": "Ativa", "zh-Hans": "活跃"},
  "profile_category_edit_title": {"en": "Edit Category", "id": "Ubah Kategori", "ar": "تعديل الفئة", "de": "Kategorie bearbeiten", "es": "Editar Categoría", "fr": "Modifier la catégorie", "hi": "श्रेणी संपादित करें", "ja": "カテゴリを編集", "ko": "카테고리 편집", "pt-BR": "Editar Categoria", "zh-Hans": "编辑类别"},
  "profile_category_empty": {"en": "No custom categories", "id": "Tidak ada kategori kustom", "ar": "لا توجد فئات مخصصة", "de": "Keine benutzerdefinierten Kategorien", "es": "Sin categorías personalizadas", "fr": "Aucune catégorie personnalisée", "hi": "कोई कस्टम श्रेणियां नहीं", "ja": "カスタムカテゴリなし", "ko": "사용자 카테고리 없음", "pt-BR": "Sem categorias personalizadas", "zh-Hans": "无自定义类别"},
  "profile_category_inactive": {"en": "Inactive", "id": "Tidak Aktif", "ar": "غير نشط", "de": "Inaktiv", "es": "Inactiva", "fr": "Inactif", "hi": "निष्क्रिय", "ja": "無効", "ko": "비활성", "pt-BR": "Inativa", "zh-Hans": "不活跃"},
  "profile_category_name": {"en": "Category Name", "id": "Nama Kategori", "ar": "اسم الفئة", "de": "Kategoriename", "es": "Nombre de Categoría", "fr": "Nom de la catégorie", "hi": "श्रेणी का नाम", "ja": "カテゴリ名", "ko": "카테고리 이름", "pt-BR": "Nome da Categoria", "zh-Hans": "类别名称"},
  "profile_category_new_title": {"en": "New Category", "id": "Kategori Baru", "ar": "فئة جديدة", "de": "Neue Kategorie", "es": "Nueva Categoría", "fr": "Nouvelle catégorie", "hi": "नई श्रेणी", "ja": "新しいカテゴリ", "ko": "새 카테고리", "pt-BR": "Nova Categoria", "zh-Hans": "新类别"},
  "profile_category_system_title": {"en": "System Categories", "id": "Kategori Sistem", "ar": "فئات النظام", "de": "Systemkategorien", "es": "Categorías del Sistema", "fr": "Catégories système", "hi": "सिस्टम श्रेणियां", "ja": "システムカテゴリ", "ko": "시스템 카테고리", "pt-BR": "Categorias do Sistema", "zh-Hans": "系统类别"},
  "profile_category_user_title": {"en": "User Categories", "id": "Kategori Pengguna", "ar": "فئات المستخدم", "de": "Benutzerkategorien", "es": "Categorías del Usuario", "fr": "Catégories utilisateur", "hi": "उपयोगकर्ता श्रेणियां", "ja": "ユーザーカテゴリ", "ko": "사용자 카테고리", "pt-BR": "Categorias do Usuário", "zh-Hans": "用户类别"},
  "profile_delete_confirm_button": {"ar": "حذف الحساب نهائيًا", "de": "Konto endgültig löschen", "en": "Delete Account Permanently", "es": "Eliminar Cuenta Permanentemente", "fr": "Supprimer le compte définitivement", "hi": "खाता स्थायी रूप से हटाएं", "id": "Hapus Akun Secara Permanen", "ja": "アカウントを完全に削除", "ko": "계정 영구 삭제", "pt-BR": "Excluir Conta Permanentemente", "zh-Hans": "永久删除账户"},
  "profile_delete_confirm_message": {"ar": "هل أنت متأكد من حذف حسابك؟\n\nسيتم حذف جميع بياناتك نهائيًا.", "de": "Möchten Sie Ihr Konto wirklich löschen?\n\nAlle Daten werden dauerhaft gelöscht.", "en": "Are you sure you want to delete your account?\n\nThis will permanently delete:\n• All your transactions\n• All your budgets\n• All your settings\n• Your profile data\n\nThis action cannot be undone.", "es": "¿Estás seguro de eliminar tu cuenta?\n\nTodos tus datos se eliminarán permanentemente.", "fr": "Voulez-vous supprimer votre compte ?\n\nToutes vos données seront supprimées définitivement.", "hi": "क्या आप अपना खाता हटाना चाहते हैं?\n\nसभी डेटा स्थायी रूप से हटा दिया जाएगा।", "id": "Apakah Anda yakin ingin menghapus akun Anda?\n\nIni akan menghapus secara permanen:\n• Semua transaksi Anda\n• Semua anggaran Anda\n• Semua pengaturan Anda\n• Data profil Anda\n\nTindakan ini tidak dapat dibatalkan.", "ja": "アカウントを削除してもよろしいですか？\n\nすべてのデータが永久に削除されます。", "ko": "계정을 삭제하시겠습니까?\n\n모든 데이터가 영구적으로 삭제됩니다.", "pt-BR": "Tem certeza que deseja excluir sua conta?\n\nTodos os seus dados serão excluídos permanentemente.", "zh-Hans": "确定要删除您的账户吗？\n\n所有数据将被永久删除。"},
  "profile_delete_confirm_title": {"ar": "حذف الحساب", "de": "Konto löschen", "en": "Delete Account", "es": "Eliminar Cuenta", "fr": "Supprimer le compte", "hi": "खाता हटाएं", "id": "Hapus Akun", "ja": "アカウントを削除", "ko": "계정 삭제", "pt-BR": "Excluir Conta", "zh-Hans": "删除账户"},
  "profile_edit_email": {"ar": "البريد الإلكتروني", "de": "E-Mail", "en": "Email", "es": "Correo Electrónico", "fr": "E-mail", "hi": "ईमेल", "id": "Email", "ja": "メール", "ko": "이메일", "pt-BR": "E-mail", "zh-Hans": "电子邮件"},
  "profile_edit_name": {"ar": "الاسم", "de": "Name", "en": "Name", "es": "Nombre", "fr": "Nom", "hi": "नाम", "id": "Nama", "ja": "名前", "ko": "이름", "pt-BR": "Nome", "zh-Hans": "姓名"},
  "profile_edit_personal_info": {"ar": "المعلومات الشخصية", "de": "Persönliche Informationen", "en": "Personal Information", "es": "Información Personal", "fr": "Informations personnelles", "hi": "व्यक्तिगत जानकारी", "id": "Informasi Pribadi", "ja": "個人情報", "ko": "개인 정보", "pt-BR": "Informações Pessoais", "zh-Hans": "个人信息"},
  "profile_edit_phone": {"ar": "الهاتف", "de": "Telefon", "en": "Phone", "es": "Teléfono", "fr": "Téléphone", "hi": "फ़ोन", "id": "Telepon", "ja": "電話", "ko": "전화번호", "pt-BR": "Telefone", "zh-Hans": "电话"},
  "profile_edit_title": {"ar": "تعديل الملف الشخصي", "de": "Profil bearbeiten", "en": "Edit Profile", "es": "Editar Perfil", "fr": "Modifier le profil", "hi": "प्रोफ़ाइल संपादित करें", "id": "Edit Profil", "ja": "プロフィールを編集", "ko": "프로필 편집", "pt-BR": "Editar Perfil", "zh-Hans": "编辑个人资料"},
  "profile_empty_no_profile": {"ar": "لم يتم تحميل الملف الشخصي", "de": "Kein Profil geladen", "en": "No profile loaded", "es": "Perfil no cargado", "fr": "Aucun profil chargé", "hi": "कोई प्रोफ़ाइल लोड नहीं हुई", "id": "Profil tidak dimuat", "ja": "プロフィールがロードされていません", "ko": "프로필이 로드되지 않았습니다", "pt-BR": "Nenhum perfil carregado", "zh-Hans": "未加载个人资料"},
  "profile_error_add_category_failed": {"ar": "فشل إضافة الفئة: %s", "de": "Kategorie hinzufügen fehlgeschlagen: %s", "en": "Failed to add category: %s", "es": "Error al agregar categoría: %s", "fr": "Échec de l'ajout de la catégorie : %s", "hi": "श्रेणी जोड़ने में विफल: %s", "id": "Gagal menambahkan kategori: %s", "ja": "カテゴリの追加に失敗: %s", "ko": "카테고리 추가 실패: %s", "pt-BR": "Falha ao adicionar categoria: %s", "zh-Hans": "添加类别失败：%s"},
  "profile_error_delete_category_failed": {"ar": "فشل حذف الفئة: %s", "de": "Kategorie löschen fehlgeschlagen: %s", "en": "Failed to delete category: %s", "es": "Error al eliminar categoría: %s", "fr": "Échec de la suppression de la catégorie : %s", "hi": "श्रेणी हटाने में विफल: %s", "id": "Gagal menghapus kategori: %s", "ja": "カテゴリの削除に失敗: %s", "ko": "카테고리 삭제 실패: %s", "pt-BR": "Falha ao excluir categoria: %s", "zh-Hans": "删除类别失败：%s"},
  "profile_error_delete_failed": {"ar": "فشل حذف الحساب", "de": "Konto löschen fehlgeschlagen", "en": "Failed to delete account", "es": "Error al eliminar la cuenta", "fr": "Échec de la suppression du compte", "hi": "खाता हटाने में विफल", "id": "Gagal menghapus akun", "ja": "アカウントの削除に失敗", "ko": "계정 삭제 실패", "pt-BR": "Falha ao excluir conta", "zh-Hans": "删除账户失败"},
  "profile_error_fetch_categories_failed": {"ar": "فشل في جلب الفئات: %s", "de": "Kategorien laden fehlgeschlagen: %s", "en": "Failed to fetch categories: %s", "es": "Error al obtener categorías: %s", "fr": "Échec du chargement des catégories : %s", "hi": "श्रेणियां प्राप्त करने में विफल: %s", "id": "Gagal mengambil kategori: %s", "ja": "カテゴリの取得に失敗: %s", "ko": "카테고리 로드 실패: %s", "pt-BR": "Falha ao buscar categorias: %s", "zh-Hans": "获取类别失败：%s"},
  "profile_error_offline_cached": {"ar": "عرض الملف الشخصي المخزن (غير متصل)", "de": "Zwischengespeichertes Profil wird angezeigt (offline)", "en": "Viewing cached profile (offline)", "es": "Visualizando perfil en caché (sin conexión)", "fr": "Profil en cache affiché (hors ligne)", "hi": "कैश्ड प्रोफ़ाइल देख रहे हैं (ऑफ़लाइन)", "id": "Melihat profil tersimpan (offline)", "ja": "キャッシュされたプロフィールを表示中（オフライン）", "ko": "캐시된 프로필 보기 (오프라인)", "pt-BR": "Visualizando perfil em cache (offline)", "zh-Hans": "查看缓存的个人资料（离线）"},
  "profile_error_online_required": {"ar": "يتطلب تعديل الملف الشخصي اتصالًا بالإنترنت", "de": "Profiländerungen erfordern eine Internetverbindung", "en": "Profile updates require an internet connection", "es": "Las actualizaciones del perfil requieren conexión a internet", "fr": "Les mises à jour du profil nécessitent une connexion internet", "hi": "प्रोफ़ाइल अपडेट के लिए इंटरनेट कनेक्शन आवश्यक है", "id": "Pembaruan profil memerlukan koneksi internet", "ja": "プロフィールの更新にはインターネット接続が必要です", "ko": "프로필 업데이트에는 인터넷 연결이 필요합니다", "pt-BR": "Atualizações de perfil requerem conexão com a internet", "zh-Hans": "更新个人资料需要网络连接"},
  "profile_error_update_category_failed": {"ar": "فشل تحديث الفئة: %s", "de": "Kategorie aktualisieren fehlgeschlagen: %s", "en": "Failed to update category: %s", "es": "Error al actualizar categoría: %s", "fr": "Échec de la mise à jour de la catégorie : %s", "hi": "श्रेणी अपडेट करने में विफल: %s", "id": "Gagal memperbarui kategori: %s", "ja": "カテゴリの更新に失敗: %s", "ko": "카테고리 업데이트 실패: %s", "pt-BR": "Falha ao atualizar categoria: %s", "zh-Hans": "更新类别失败：%s"},
  "profile_menu_goal_tracker": {"ar": "متتبع الأهداف", "de": "Ziel-Tracker", "en": "Goal Tracker", "es": "Rastreador de Metas", "fr": "Suivi des objectifs", "hi": "लक्ष्य ट्रैकर", "id": "Pelacak Tujuan", "ja": "目標トラッカー", "ko": "목표 추적기", "pt-BR": "Rastreador de Metas", "zh-Hans": "目标追踪"},
  "profile_menu_liabilities": {"ar": "الالتزامات", "de": "Verbindlichkeiten", "en": "Liabilities", "es": "Pasivos", "fr": "Passifs", "hi": "देनदारियां", "id": "Liabilitas", "ja": "負債", "ko": "부채", "pt-BR": "Passivos", "zh-Hans": "负债"},
  "profile_menu_manage_categories": {"ar": "إدارة الفئات", "de": "Kategorien verwalten", "en": "Manage Categories", "es": "Gestionar Categorías", "fr": "Gérer les catégories", "hi": "श्रेणियां प्रबंधित करें", "id": "Kelola Kategori", "ja": "カテゴリを管理", "ko": "카테고리 관리", "pt-BR": "Gerenciar Categorias", "zh-Hans": "管理类别"},
  "profile_menu_portfolio": {"ar": "المحفظة", "de": "Portfolio", "en": "Portfolio", "es": "Portafolio", "fr": "Portefeuille", "hi": "पोर्टफोलियो", "id": "Portofolio", "ja": "ポートフォリオ", "ko": "포트폴리오", "pt-BR": "Portfólio", "zh-Hans": "投资组合"},
  "profile_navigation_title": {"ar": "الملف الشخصي", "de": "Profil", "en": "Profile", "es": "Perfil", "fr": "Profil", "hi": "प्रोफ़ाइल", "id": "Profil", "ja": "プロフィール", "ko": "프로필", "pt-BR": "Perfil", "zh-Hans": "个人资料"},
  "profile_notifications_clear_all": {"ar": "مسح الكل", "de": "Alle löschen", "en": "Clear All", "es": "Limpiar Todo", "fr": "Tout effacer", "hi": "सब साफ करें", "id": "Hapus Semua", "ja": "すべてクリア", "ko": "모두 지우기", "pt-BR": "Limpar Tudo", "zh-Hans": "清除全部"},
  "profile_notifications_empty_message": {"ar": "ستظهر الإشعارات هنا", "de": "Benachrichtigungen werden hier angezeigt", "en": "Notifications you receive will appear here", "es": "Las notificaciones aparecerán aquí", "fr": "Les notifications apparaîtront ici", "hi": "सूचनाएं यहां दिखाई देंगी", "id": "Notifikasi yang Anda terima akan muncul di sini", "ja": "通知はここに表示されます", "ko": "알림이 여기에 표시됩니다", "pt-BR": "Notificações aparecerão aqui", "zh-Hans": "通知会显示在这里"},
  "profile_notifications_empty_title": {"ar": "لا توجد إشعارات", "de": "Keine Benachrichtigungen", "en": "No Notifications", "es": "Sin Notificaciones", "fr": "Aucune notification", "hi": "कोई सूचनाएं नहीं", "id": "Tidak Ada Notifikasi", "ja": "通知なし", "ko": "알림 없음", "pt-BR": "Sem Notificações", "zh-Hans": "暂无通知"},
  "profile_notifications_mark_all_read": {"ar": "تعليم الكل كمقروء", "de": "Alle als gelesen markieren", "en": "Mark All as Read", "es": "Marcar Todo como Leído", "fr": "Tout marquer comme lu", "hi": "सभी पढ़ा हुआ चिह्नित करें", "id": "Tandai Semua Dibaca", "ja": "すべて既読にする", "ko": "모두 읽음 표시", "pt-BR": "Marcar Tudo como Lido", "zh-Hans": "全部标为已读"},
  "profile_section_account_settings": {"ar": "إعدادات الحساب", "de": "Kontoeinstellungen", "en": "Account Settings", "es": "Ajustes de Cuenta", "fr": "Paramètres du compte", "hi": "खाता सेटिंग्स", "id": "Pengaturan Akun", "ja": "アカウント設定", "ko": "계정 설정", "pt-BR": "Configurações da Conta", "zh-Hans": "账户设置"},
  "profile_section_developer_tools": {"ar": "أدوات المطور", "de": "Entwicklerwerkzeuge", "en": "Developer Tools", "es": "Herramientas de Desarrollo", "fr": "Outils de développement", "hi": "डेवलपर टूल्स", "id": "Alat Pengembang", "ja": "開発者ツール", "ko": "개발자 도구", "pt-BR": "Ferramentas de Desenvolvedor", "zh-Hans": "开发者工具"},
  "profile_section_financial_liberty": {"ar": "الحرية المالية", "de": "Finanzielle Freiheit", "en": "Financial Liberty", "es": "Libertad Financiera", "fr": "Liberté financière", "hi": "वित्तीय स्वतंत्रता", "id": "Kebebasan Finansial", "ja": "ファイナンシャルリバティ", "ko": "재정적 자유", "pt-BR": "Liberdade Financeira", "zh-Hans": "财务自由"},
  "profile_section_subscription": {"ar": "الاشتراك", "de": "Abonnement", "en": "Subscription", "es": "Suscripción", "fr": "Abonnement", "hi": "सदस्यता", "id": "Langganan", "ja": "サブスクリプション", "ko": "구독", "pt-BR": "Assinatura", "zh-Hans": "订阅"},
  "profile_status_free": {"ar": "مجاني", "de": "Kostenlos", "en": "Free", "es": "Gratuito", "fr": "Gratuit", "hi": "मुफ्त", "id": "Gratis", "ja": "無料", "ko": "무료", "pt-BR": "Gratuito", "zh-Hans": "免费"},
  "profile_status_premium": {"ar": "Premium", "de": "Premium", "en": "Premium", "es": "Premium", "fr": "Premium", "hi": "Premium", "id": "Premium", "ja": "Premium", "ko": "프리미엄", "pt-BR": "Premium", "zh-Hans": "Premium"},
  "profile_subscription_active": {"ar": "نشط", "de": "AKTIV", "en": "ACTIVE", "es": "ACTIVO", "fr": "ACTIF", "hi": "सक्रिय", "id": "AKTIF", "ja": "有効", "ko": "활성", "pt-BR": "ATIVO", "zh-Hans": "活跃"},
  "profile_subscription_inactive": {"ar": "غير نشط", "de": "INAKTIV", "en": "INACTIVE", "es": "INACTIVO", "fr": "INACTIF", "hi": "निष्क्रिय", "id": "TIDAK AKTIF", "ja": "無効", "ko": "비활성", "pt-BR": "INATIVO", "zh-Hans": "不活跃"},
  "profile_subscription_status": {"ar": "حالة Premium", "de": "Premium-Status", "en": "Premium Status", "es": "Estado Premium", "fr": "Statut Premium", "hi": "Premium स्थिति", "id": "Status Premium", "ja": "Premiumステータス", "ko": "프리미엄 상태", "pt-BR": "Status Premium", "zh-Hans": "Premium 状态"},
  "profile_label_photo_title": {"ar": "صورة الملف الشخصي", "de": "Profilbild", "en": "Profile Photo", "es": "Foto de Perfil", "fr": "Photo de profil", "hi": "प्रोफ़ाइल फ़ोटो", "id": "Foto Profil", "ja": "プロフィール写真", "ko": "프로필 사진", "pt-BR": "Foto do Perfil", "zh-Hans": "个人资料照片"}
}

LOCALE_MAP = {
    'en': 'values',
    'id': 'values-in',
    'ar': 'values-ar',
    'de': 'values-de',
    'es': 'values-es',
    'fr': 'values-fr',
    'hi': 'values-hi',
    'ja': 'values-ja',
    'ko': 'values-ko',
    'pt-BR': 'values-pt-rBR',
    'zh-Hans': 'values-zh-rCN'
}

base_dir = '/Users/ptsiagaabdiutama/Documents/Casha-android/app/src/main/res'

for locale_code, dir_name in LOCALE_MAP.items():
    strings_file = os.path.join(base_dir, dir_name, 'strings.xml')
    if not os.path.exists(strings_file):
        print(f"File not found: {strings_file}")
        continue

    try:
        tree = ET.parse(strings_file)
        resources = tree.getroot()

        # Check if comment exists
        has_comment = False
        for child in resources:
            if "PROFILE" in str(child).upper():
                has_comment = True
                break

        # Remove existing profile strings
        elements_to_remove = []
        for child in resources.findall('string'):
            if child.get('name', '').startswith('profile_'):
                elements_to_remove.append(child)
        for elem in elements_to_remove:
            resources.remove(elem)

        # Add comment
        if not has_comment:
            resources.append(ET.Comment(' Profile '))

        # Add new strings
        for key, locales in TRANSLATIONS.items():
            if locale_code in locales:
                val = locales[locale_code]
                val = val.replace("'", "\\'") # escape quotes
                val = val.replace("\n", "\\n") # preserve newlines
                
                string_elem = ET.Element('string')
                string_elem.set('name', key)
                string_elem.text = val
                resources.append(string_elem)

        # Pretty print with indents
        rough_string = ET.tostring(resources, 'utf-8')
        reparsed = minidom.parseString(rough_string)
        # Ensure we don't have empty lines between all tags and it's nicely indented
        pretty_str = reparsed.toprettyxml(indent="    ")
        lines = [line for line in pretty_str.split('\n') if line.strip()]
        
        with open(strings_file, 'w', encoding='utf-8') as f:
            f.write('\n'.join(lines) + '\n')
            
        print(f"Updated {strings_file}")

    except Exception as e:
        print(f"Error updating {strings_file}: {e}")
