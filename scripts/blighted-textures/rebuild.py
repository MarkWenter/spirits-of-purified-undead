"""Rebuild native-UV blighted textures from calibrated imagegen masks.
Requires Pillow and numpy; supply your legally installed vanilla client JAR(s).
No old blighted texture is used as input. Boss textures are outside the target tree.
"""
from pathlib import Path
from PIL import Image
import argparse, numpy as np, zipfile, io, json, hashlib
parser=argparse.ArgumentParser()
parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[2])
parser.add_argument('--forge-jar', type=Path)
parser.add_argument('--neo-jar', type=Path)
args=parser.parse_args()
if not args.forge_jar and not args.neo_jar: parser.error('Supply at least one vanilla client JAR')
R=args.root; A=Path(__file__).resolve().parent
spec=json.loads((A/'spec.json').read_text(encoding='utf8'))
jars={k:v for k,v in [('forge',args.forge_jar),('neo',args.neo_jar)] if v}
masks={n:Image.open(A/'masks'/(n+'.png')).convert('RGBA') for n in spec}
def original(z,p): return Image.open(io.BytesIO(z.read('assets/minecraft/textures/entity/'+p))).convert('RGBA')
def eyes(n):
 if n in ['zombie','skeleton','husk','stray','drowned','bogged','bogged_overlay']:return [(9,12),(10,12),(13,12),(14,12)]
 if n in ['villager','pillager','vindicator','witch']:return [(10,14),(13,14)]
 return {'pig':[(8,11),(15,11)],'cow':[(6,9),(7,9),(12,9),(13,9)],'sheep':[(8,10),(13,10)],'chicken':[(3,4),(6,4)],'horse':[(3,21),(4,21),(15,21),(16,21)],'donkey':[(3,21),(4,21),(15,21),(16,21)]}.get(n,[])
def compose(src,m,n,layer=False):
 # Only low-strength grade: original coat, clothes, patch outlines remain recognizable.
 arr=np.array(src).copy();rgb=arr[:,:,:3].astype(float);gray=rgb.mean(axis=2,keepdims=True);arr[:,:,:3]=np.clip((rgb*.92+gray*.08)*.92,0,255).astype('uint8')
 out=Image.fromarray(arr);mask=Image.new('RGBA',src.size);mask.paste(m,(0,0));out=Image.alpha_composite(out,mask);out.putalpha(src.getchannel('A'))
 for i,(x,y) in enumerate(eyes(n)):
  if x<src.width and y<src.height and src.getpixel((x,y))[3]:out.putpixel((x,y),((222,24,35,255) if i%2==0 else (170,15,25,255)))
 return out
summary={};by_path={p:n for n,p in spec.items()};previews=[]
for label,root in [(k, R if k=='forge' else R/'1.21.1neoforge版本') for k in jars]:
 target=root/'src/main/resources/assets/purified_undead/textures/entity/blighted';changed=[]
 with zipfile.ZipFile(jars[label]) as z:
  for dest in target.rglob('*.png'):
   p=dest.relative_to(target).as_posix();src=original(z,p);n=by_path.get(p);out=src.copy();mode='vanilla-restored'
   if n:out=compose(src,masks[n],n);mode='imagegen-calibrated'
   elif p.startswith('horse/horse_') and 'markings' not in p and p not in ['horse/horse_skeleton.png','horse/horse_zombie.png']:
    out=compose(src,masks['horse'],'horse');mode='coat-variant'
   elif p.startswith('horse/horse_markings_'):
    out=compose(src,masks['horse'],'');mode='markings-overlay'
   elif p.startswith('zombie_villager/') and '/profession_level/' not in p:
    cloth=masks['villager_clothes'].copy();cloth.alpha_composite(masks['villager'].crop((0,0,32,18)),(0,0))
    out=compose(src,cloth,'');mode='clothing-overlay'
   # Restore saddle/tack exactly, even the color grade is excluded.
   if p.startswith('horse/') and mode!='vanilla-restored':
    for box in [(0,0,src.width,13),(26,20,48,32)]:out.paste(src.crop(box),box)
   assert out.size==src.size and out.getchannel('A').tobytes()==src.getchannel('A').tobytes()
   out.save(dest);changed.append({'path':p,'mode':mode,'sha256':hashlib.sha256(dest.read_bytes()).hexdigest()})
   if n and (label=='forge' or n.startswith('bogged')):previews.append((n,src,out))
 summary[label]=changed
print(json.dumps({k:len(v) for k,v in summary.items()}))
