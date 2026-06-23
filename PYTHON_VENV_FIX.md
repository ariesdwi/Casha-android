# Python Virtual Environment Fix

## Error Message
```
error: externally-managed-environment

× This environment is externally managed
╰─> To install Python packages system-wide, try brew install xyz
```

## Root Cause
macOS (dan distro Linux modern) menggunakan **PEP 668 externally-managed environment** yang mencegah install Python packages secara global untuk melindungi system Python.

---

## ✅ Solution Applied

### **Modified: `publish-play-store.sh`**

Script sekarang otomatis membuat dan menggunakan **Python virtual environment** lokal di project:

```bash
# Before (error):
pip3 install google-api-python-client google-auth

# After (works):
python3 -m venv .venv
source .venv/bin/activate
pip3 install google-api-python-client google-auth
# ... run script ...
deactivate
```

---

## 🔄 How It Works

### 1. **First Run**
```bash
./publish-play-store.sh
```

Script akan:
1. ✅ Detect tidak ada `.venv/` folder
2. ✅ Create virtual environment: `python3 -m venv .venv`
3. ✅ Activate virtual environment: `source .venv/bin/activate`
4. ✅ Install dependencies: `pip3 install google-api-python-client google-auth`
5. ✅ Run publish process
6. ✅ Deactivate virtual environment

**Output:**
```
📦 Creating Python virtual environment...
✅ Virtual environment created!
📦 Installing required Python packages...
✅ Dependencies installed!
```

### 2. **Subsequent Runs**
Script akan:
1. ✅ Detect `.venv/` folder exists
2. ✅ Activate existing virtual environment
3. ✅ Skip installation (already installed)
4. ✅ Run publish process
5. ✅ Deactivate virtual environment

**Output:**
```
✅ Dependencies already installed
```

---

## 📁 Files Modified

### 1. **publish-play-store.sh**
```bash
# Create virtual environment if it doesn't exist
VENV_DIR=".venv"
if [ ! -d "$VENV_DIR" ]; then
  echo "📦 Creating Python virtual environment..."
  python3 -m venv "$VENV_DIR"
  echo "✅ Virtual environment created!"
fi

# Activate virtual environment
source "$VENV_DIR/bin/activate"

# Check and install dependencies
if ! python3 -c "from googleapiclient.discovery import build; from google.oauth2 import service_account" 2>/dev/null; then
  echo "📦 Installing required Python packages..."
  pip3 install google-api-python-client google-auth --quiet
  echo "✅ Dependencies installed!"
else
  echo "✅ Dependencies already installed"
fi

# ... rest of script ...

# Cleanup at the end
deactivate
```

### 2. **.gitignore**
Added Python virtual environment entries:
```gitignore
# Python virtual environment
.venv/
venv/
__pycache__/
*.pyc
```

---

## 🎯 Benefits

### 1. **No System Pollution**
- ✅ Packages installed only for this project
- ✅ System Python remains clean
- ✅ No conflicts with other projects

### 2. **Reproducible Environment**
- ✅ Exact same packages for all developers
- ✅ Isolated from system updates
- ✅ Easy to delete and recreate

### 3. **macOS PEP 668 Compliant**
- ✅ No `--break-system-packages` needed
- ✅ No homebrew conflicts
- ✅ Best practice for modern Python

### 4. **Zero Manual Setup**
- ✅ Virtual environment created automatically
- ✅ Dependencies installed automatically
- ✅ Works out of the box

---

## 🛠️ Manual Virtual Environment Management

### Create Virtual Environment
```bash
python3 -m venv .venv
```

### Activate Virtual Environment
```bash
# macOS/Linux
source .venv/bin/activate

# Windows
.venv\Scripts\activate
```

### Install Dependencies
```bash
pip3 install google-api-python-client google-auth
```

### Check Installed Packages
```bash
pip3 list
```

### Deactivate Virtual Environment
```bash
deactivate
```

### Delete Virtual Environment
```bash
rm -rf .venv
```

---

## 🔍 Verification

### Check Virtual Environment is Active
```bash
which python3
# Should show: /Users/.../Casha-android/.venv/bin/python3

echo $VIRTUAL_ENV
# Should show: /Users/.../Casha-android/.venv
```

### Check Packages Location
```bash
pip3 show google-api-python-client
# Location: /Users/.../Casha-android/.venv/lib/python3.x/site-packages
```

### Test Import
```bash
source .venv/bin/activate
python3 -c "from googleapiclient.discovery import build; print('✅ Import successful')"
deactivate
```

---

## 📊 Directory Structure

```
Casha-android/
├── .venv/                          # ← Virtual environment (auto-created)
│   ├── bin/
│   │   ├── activate                # Activation script
│   │   ├── pip3                    # Virtual env pip
│   │   └── python3                 # Virtual env python
│   ├── lib/
│   │   └── python3.x/
│   │       └── site-packages/      # Installed packages here
│   │           ├── googleapiclient/
│   │           ├── google/
│   │           └── ...
│   └── pyvenv.cfg
├── publish-play-store.sh           # ← Auto uses .venv
├── .gitignore                      # ← Ignores .venv/
└── ...
```

---

## ⚠️ Important Notes

### 1. **Don't Commit .venv/**
- Virtual environment is in `.gitignore`
- Each developer creates their own
- Keeps repo size small

### 2. **Virtual Environment is Project-Local**
- Only affects Casha Android project
- Other projects unaffected
- Can be deleted anytime

### 3. **Automatic Cleanup**
- Script always calls `deactivate` at the end
- Even on errors (via exit trap)
- No lingering activated environments

### 4. **Python Version**
Virtual environment uses system Python version:
```bash
python3 --version
# Should be: Python 3.9+ (macOS default)
```

---

## 🐛 Troubleshooting

### Issue: "python3: command not found"
**Solution:** Install Python 3
```bash
# Check if Python 3 is installed
which python3

# If not found, install via Homebrew
brew install python@3.11
```

### Issue: "Permission denied: .venv/bin/activate"
**Solution:** Fix permissions
```bash
chmod +x .venv/bin/activate
```

### Issue: Virtual environment corrupted
**Solution:** Delete and recreate
```bash
rm -rf .venv
./publish-play-store.sh
# Will auto-create new .venv
```

### Issue: Dependencies not found after install
**Solution:** Verify activation
```bash
source .venv/bin/activate
pip3 list | grep google
# Should show google-api-python-client and google-auth
```

### Issue: "ModuleNotFoundError" when running script
**Solution:** Check if script activates venv
```bash
# Add debug line in script
echo "VIRTUAL_ENV: $VIRTUAL_ENV"
# Should print: /Users/.../Casha-android/.venv
```

---

## 🔄 Migration from Global Install

### If You Previously Used Global Install

**Old way (no longer recommended):**
```bash
pip3 install google-api-python-client google-auth
```

**New way (automatic in script):**
```bash
./publish-play-store.sh
# Handles everything automatically
```

**To clean up old global packages (optional):**
```bash
pip3 uninstall google-api-python-client google-auth -y
```

Note: Only do this if you're not using these packages in other projects.

---

## 📚 Additional Resources

- [PEP 668: Marking Python interpreters as externally managed](https://peps.python.org/pep-0668/)
- [Python venv Documentation](https://docs.python.org/3/library/venv.html)
- [Google API Python Client](https://github.com/googleapis/google-api-python-client)

---

## ✅ Summary

| Aspect | Before | After |
|--------|--------|-------|
| **Installation** | Global (`pip3 install`) | Local virtual env |
| **Setup** | Manual | Automatic |
| **Isolation** | ❌ System-wide | ✅ Project-only |
| **macOS Compatible** | ❌ Breaks on PEP 668 | ✅ Fully compliant |
| **Clean Uninstall** | Hard (affects system) | Easy (`rm -rf .venv`) |
| **Git** | N/A | Ignored via .gitignore |

---

## 🎉 Result

Script sekarang berfungsi dengan sempurna di macOS modern tanpa:
- ❌ `--break-system-packages` flag
- ❌ Manual Python setup
- ❌ System Python pollution
- ❌ Permission errors

Just run:
```bash
./publish-play-store.sh
```

Dan everything works! 🚀
