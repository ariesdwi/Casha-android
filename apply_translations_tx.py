import json
import os
import re

# Android locale folder mapping
locale_map = {
    "ar": "values-ar",
    "de": "values-de",
    "en": "values",
    "es": "values-es",
    "fr": "values-fr",
    "hi": "values-hi",
    "id": "values-in",
    "ja": "values-ja",
    "ko": "values-ko",
    "pt-BR": "values-pt-rBR",
    "zh-Hans": "values-zh-rCN"
}

def escape_android_string(value):
    # Escape quotes and apostrophes
    value = value.replace("'", "\\'").replace('"', '\\"')
    # Replace %s with %s (or %d depending on context) — the input already uses %s, %1$s, %d
    # But Android strings need literal '&' to be '&amp;'
    value = value.replace("&", "&amp;")
    # Replace ... with &#8230; (ellipsis)
    value = value.replace("...", "&#8230;")
    # Replace iOS style %@ with Android style %s
    value = value.replace("%@", "%s")
    # Replace iOS style %lld with Android style %d
    value = value.replace("%lld", "%d")
    return value

def format_key(key):
    # 'transactions.detail.amount_received' -> 'transactions_detail_amount_received'
    # 'transactions.results_found %@' -> 'transactions_results_found'
    key = re.sub(r' %[@llds]+', '', key)
    return key.replace(".", "_")

def main():
    with open('/tmp/transactions.json', 'r') as f:
        data = json.load(f)
    
    strings = data.get("strings", {})
    
    # Group translations by locale
    locale_strings = {locale: {} for locale in locale_map.values()}
    for locale in locale_map.values():
        locale_strings[locale] = {}

    for key, val in strings.items():
        android_key = format_key(key)
        localizations = val.get("localizations", {})
        
        for lang, lang_data in localizations.items():
            if lang in locale_map:
                folder = locale_map[lang]
                value = lang_data.get("stringUnit", {}).get("value", "")
                locale_strings[folder][android_key] = escape_android_string(value)

    res_dir = "app/src/main/res"

    for folder, key_values in locale_strings.items():
        strings_xml_path = os.path.join(res_dir, folder, "strings.xml")
        
        if not os.path.exists(strings_xml_path):
            print(f"Skipping {strings_xml_path} - not found")
            continue
            
        with open(strings_xml_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Find the closing </resources> tag
        end_idx = content.rfind("</resources>")
        if end_idx == -1:
            print(f"Malformed {strings_xml_path}")
            continue

        # Insert new strings before closing tag
        new_nodes = []
        for k, v in key_values.items():
            # Avoid inserting if it's already there
            if f'name="{k}"' not in content:
                new_nodes.append(f'    <string name="{k}">{v}</string>')
        
        if new_nodes:
            new_content = content[:end_idx] + "\n    <!-- Transaction Module -->\n" + "\n".join(new_nodes) + "\n" + content[end_idx:]
            with open(strings_xml_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Updated {strings_xml_path} with {len(new_nodes)} string(s)")
        else:
            print(f"No new strings to add for {strings_xml_path}")

if __name__ == "__main__":
    main()
