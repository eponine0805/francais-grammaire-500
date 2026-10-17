from PIL import Image,ImageDraw,ImageFont
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
font='/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf'
font2='/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'
for density,size in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
    S=512
    im=Image.new('RGB',(S,S),'#FAFAFA');d=ImageDraw.Draw(im)
    d.rounded_rectangle((10,10,502,502),radius=100,fill='white',outline='#ECECEC',width=8)
    d.rounded_rectangle((35,33,475,445),radius=55,fill='#171819')
    f=ImageFont.truetype(font,295)
    glyph='G';box=d.textbbox((0,0),glyph,font=f)
    d.text(((S-(box[2]-box[0]))/2-box[0],(430-(box[3]-box[1]))/2-box[1]-8),glyph,font=f,fill='white')
    band=[('#183c93',35,181),('#FFFFFF',181,327),('#EC273C',327,475)]
    for color,x0,x1 in band:d.rectangle((x0,446,x1,481),fill=color)
    out=ROOT/f'app/src/main/res/mipmap-{density}/ic_launcher.png'
    out.parent.mkdir(parents=True,exist_ok=True)
    im.resize((size,size),Image.Resampling.LANCZOS).save(out)
print('Launcher icons generated')
