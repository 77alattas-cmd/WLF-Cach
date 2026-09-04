import os
import re

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # We want to find OutlinedTextField calls that contain KeyboardType.Number
    # and replace OutlinedTextField with com.example.ui.components.AppNumberField
    
    # A simple regex to find OutlinedTextField blocks and check if they have KeyboardType.Number
    # Since they can span multiple lines, we'll split by OutlinedTextField(
    
    parts = content.split('OutlinedTextField(')
    new_content = parts[0]
    
    for part in parts[1:]:
        # Find the matching closing parenthesis
        depth = 1
        i = 0
        while i < len(part) and depth > 0:
            if part[i] == '(':
                depth += 1
            elif part[i] == ')':
                depth -= 1
            i += 1
            
        block = part[:i]
        remainder = part[i:]
        
        if 'KeyboardType.Number' in block:
            new_content += 'com.example.ui.components.AppNumberField(' + block + remainder
        else:
            new_content += 'OutlinedTextField(' + block + remainder

    with open(filepath, 'w') as f:
        f.write(new_content)

for root, dirs, files in os.walk('app/src/main/java/com/example/ui'):
    for file in files:
        if file.endswith('.kt'):
            process_file(os.path.join(root, file))

print("Done")
