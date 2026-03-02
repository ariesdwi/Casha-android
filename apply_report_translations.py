import json
import os
import xml.etree.ElementTree as ET
from xml.dom import minidom

def sanitize_key(key):
    # report.percentage_of_total %d -> report_percentage_of_total
    key = key.replace(" %d", "").replace(" %@", "")
    return key.replace(".", "_")

def sanitize_value(value):
    # iOS type string formats to Android type string formats
    value = value.replace("%@", "%s")
    # Escape single quotes
    return value.replace("'", "\\'")

def indent(elem, level=0):
    i = "\n" + level*"    "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "    "
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
        for elem in elem:
            indent(elem, level+1)
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i

locale_map = {
    'en': 'values',
    'id': 'values-in', # Indonesian
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

with open('/tmp/report_translations.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

res_dir = "/Users/ptsiagaabdiutama/Documents/Casha-android/app/src/main/res"

strings_data = data.get("strings", {})

for locale_key, values_folder_name in locale_map.items():
    values_dir = os.path.join(res_dir, values_folder_name)
    if not os.path.exists(values_dir):
        os.makedirs(values_dir)
        print(f"Created directory: {values_dir}")

    strings_file = os.path.join(values_dir, "strings.xml")
    
    if os.path.exists(strings_file):
        tree = ET.parse(strings_file)
        root = tree.getroot()
    else:
        root = ET.Element("resources")
        tree = ET.ElementTree(root)

    # Dictionary of existing keys to avoid duplicates
    existing_keys = {child.attrib.get('name'): child for child in root.findall('string')}

    added = False
    
    # Check if we already have the section comment
    has_comment = False
    for child in root:
        if "REPORT MODULE" in str(child).upper():
             has_comment = True
             break
    
    for original_key, string_info in strings_data.items():
        localizations = string_info.get("localizations", {})
        
        # If the requested locale is in the localizations dict
        if locale_key in localizations:
            value = localizations[locale_key]["stringUnit"]["value"]
            
            sanitized_key = sanitize_key(original_key)
            sanitized_value = sanitize_value(value)

            if not has_comment and not added:
                root.append(ET.Comment(" Report Module "))
                added = True

            if sanitized_key in existing_keys:
                existing_keys[sanitized_key].text = sanitized_value
            else:
                string_elem = ET.SubElement(root, "string", name=sanitized_key)
                string_elem.text = sanitized_value

    indent(root)
    tree.write(strings_file, encoding="utf-8", xml_declaration=True)
    print(f"Updated {strings_file}")
