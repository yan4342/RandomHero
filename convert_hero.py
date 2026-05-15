import json

def convert_hero_to_string(hero):
    return f'Hero({hero["ename"]}, "{hero["cname"]}", "{hero["id_name"]}", "{hero["title"]}", {hero["new_type"]}, {hero["hero_type"]}, null, "{hero["skin_name"]}", {hero["moss_id"]})'

# 读取并解析 JSON
with open('input.txt', 'r', encoding='utf-8') as f:
    content = f.read()

# 确保是有效的 JSON 数组
if not content.strip().startswith('['):
    content = '[' + content + ']'

# 移除末尾多余的逗号（常见于手工编辑的 JSON）
import re
content = re.sub(r',\s*}', '}', content)
content = re.sub(r',\s*]', ']', content)

data = json.loads(content)

# 转换每个英雄
heroes = [convert_hero_to_string(hero) for hero in data]

# 输出
output = ',\n'.join(heroes)
print(output)

# 保存到文件
with open('output.txt', 'w', encoding='utf-8') as f:
    f.write(output)

print(f"成功转换 {len(heroes)} 个英雄")