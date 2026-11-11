export const handleColorReversal = (color: string, ilighten: number = 0): string => {
  if (color.startsWith('rgba')||color.startsWith('rgb')) {
    return handleRgbaReversal(color, ilighten);
  } else if (color.startsWith('#')) {
    return handleHexReversal(color, ilighten);
  } else {
    throw new Error('Unsupported color format');
  }
}
const handleHexReversal = (hex: string, ilighten: number): string => {
  hex = hex.replace('#', '');
  const max16 = Math.max(0, Math.min(15, 15 + ilighten));

  const reversedHex = hex.split('').map(char => {
    const c16 = parseInt(char, 16);  
    const c10 = Math.abs(max16 - c16);  
    return c10.toString(16);  
  }).join('');

  return `#${reversedHex}`;
}
const handleRgbaReversal = (rgba: string, ilighten: number): string => {
  const match = rgba.match(/rgba?\((\d+),\s*(\d+),\s*(\d+),?\s*([\d\.]*)?\)/);
  if (!match) throw new Error('Invalid RGBA color format');

  const [, r, g, b, a] = match.map(Number);
  const max255 = Math.max(0, Math.min(255, 255 + (ilighten * 17)));  

  const reverseColor = (c: number) => Math.max(0, Math.min(255, max255 - c));

  const reversedR = reverseColor(r);
  const reversedG = reverseColor(g);
  const reversedB = reverseColor(b);

  if (a !== undefined && !isNaN(a)) {
    return `rgba(${reversedR}, ${reversedG}, ${reversedB}, ${a})`;
  } else {
    return `rgb(${reversedR}, ${reversedG}, ${reversedB})`;
  }
}

export const toHexToRGB = (hex: string, opacity: number):string => {
  let RGBA =
    "rgba(" +
    parseInt("0x" + hex.slice(1, 3)) +
    "," +
    parseInt("0x" + hex.slice(3, 5)) +
    "," +
    parseInt("0x" + hex.slice(5, 7)) +
    "," +
    opacity +
    ")";
  return RGBA;
};
