import { writeFileSync } from 'node:fs';
import { tileFbm, smooth, lerp, TAU } from '../.tools/pixelfx/src/core/math.js';
import { encodePNG } from '../.tools/pixelfx/tools/lib/png.mjs';

// Bake the periodic wake once. The mesh supplies sweep direction, taper and lifetime.
const width = 256, height = 128;
const rgba = Buffer.alloc(width * height * 4);
const palette = [
  [0, [73, 17, 132]], [.30, [110, 27, 182]], [.65, [175, 51, 228]],
  [.87, [227, 67, 202]], [.91, [255, 151, 211]], [.935, [140, 38, 120]],
  [.955, [255, 143, 132]], [.975, [255, 238, 210]], [1, [255, 146, 130]],
  [1.025, [217, 51, 88]]
];
for (let y = 0; y < height; y++) {
  // Final row is white for independently tinted filaments and sparks.
  const radius = Math.min(1.025, y / (height - 2) * 1.025);
  const hi = palette.findIndex(([stop], i) => i > 0 && stop >= radius);
  const [low, c0] = palette[hi - 1], [high, c1] = palette[hi];
  for (let x = 0; x < width; x++) {
    const u = x / width;
    const curl = tileFbm(u * 9, radius * 4, 9, 0, 72);
    const vein = Math.abs(Math.sin(TAU * (u * 7 + radius * 1.6 + .64 * curl)));
    const smoke = smooth(.06, .76, Math.abs(curl) + .55 * vein);
    const rim = smooth(.82, .925, radius);
    const light = lerp(.67 + .33 * smoke, 1, rim);
    const color = c0.map((c, k) => Math.round(lerp(c, c1[k], (radius-low)/(high-low)) * light));
    const alpha = Math.round(255 * lerp(.12 + .88 * smoke, 1, rim));
    rgba.set(y === height-1 ? [255,255,255,255] : [...color, alpha], (y*width+x)*4);
  }
}
writeFileSync(new URL('../src/main/resources/assets/purified_undead/textures/entity/ferin_slash_wake.png', import.meta.url), encodePNG(width,height,rgba));
