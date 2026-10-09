"""Validate one version independently: python scripts/validate-diary.py [project-directory]."""
import json,sys,re,struct
from pathlib import Path
base=Path(sys.argv[1]) if len(sys.argv)>1 else Path(__file__).resolve().parents[1]
r=base/'src/main/resources';ns='purified_undead';book='white_witch_diary'
def read(p):return json.loads(p.read_text(encoding='utf-8'))
definition=read(r/f'data/{ns}/patchouli_books/{book}/book.json')
assert definition['use_resource_pack'] is True
assert definition.get('dont_generate_book',False) is False
assert definition['model']==f'{ns}:{book}'
assert definition['text_overflow_mode']=='overflow', 'Pages must fit without shrinking or truncation'
root=r/f'assets/{ns}/patchouli_books/{book}'
for lang in ['zh_cn','en_us']:
 categories={p.stem:read(p) for p in (root/lang/'categories').glob('*.json')}
 entries={p.stem:read(p) for p in (root/lang/'entries').glob('*.json')}
 assert len(categories)==6 and len(entries)==53
 assert max(categories,key=lambda k:categories[k]['sortnum'])=='future'
 assert len({c['name'] for c in categories.values()})==len(categories)
 assert len({e['name'] for e in entries.values()})==len(entries)
 pages=0
 for key,entry in entries.items():
  assert entry['category'].split(':')[1] in categories
  for page in entry['pages']:
   pages+=1;assert page['type'] in ['patchouli:text','patchouli:spotlight','patchouli:crafting','patchouli:smithing']
   text=re.sub(r'\$\([^)]*\)','',page.get('text',''))
   assert len(text)<=82,(key,len(text))
   if 'recipe' in page:
    folder='recipe' if (r/'META-INF/neoforge.mods.toml').exists() else 'recipes'
    assert (r/f'data/{ns}/{folder}/{page["recipe"].split(":")[1]}.json').exists()
 assert pages==130,pages
neo=(r/'META-INF/neoforge.mods.toml').exists()
recipe=read(r/f'data/{ns}/{"recipe" if neo else "recipes"}/{book}.json')
assert sorted(i['item'] for i in recipe['ingredients'])==['minecraft:book',f'{ns}:blight_fragment']
assert 'conditions' not in recipe and 'neoforge:conditions' not in recipe
if neo:
 assert recipe['type']=='minecraft:crafting_shapeless'
 assert recipe['result']=={'id':'patchouli:guide_book','count':1,'components':{'patchouli:book':f'{ns}:{book}'}}
else:
 assert recipe['type']=='patchouli:shapeless_book_recipe' and recipe['book']==f'{ns}:{book}'
for path,size in [('item/lily_diary.png',(64,64)),('item/white_witch_diary.png',(64,64)),('gui/white_witch_diary.png',(512,256)),('gui/diary_question.png',(16,16))]:
 p=r/f'assets/{ns}/textures/{path}';data=p.read_bytes();assert data[:8]==b'\x89PNG\r\n\x1a\n'
 assert struct.unpack('>II',data[16:24])==size
print(('NeoForge 1.21.1' if neo else 'Forge 1.20.1')+': diary resources OK (6 chapters, 53 entries, 130 pages, required Patchouli, textures)')
