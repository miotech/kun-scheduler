/**
 * hex color generator for random operator name
 */

export function operatorNameToHexColor(name: string) {
  if (!name) {
    return '#dddddd';
  }
  if (name.match(/bash/i)) {
    return '#1F9336';
  }
  if (name.match(/spark/i)) {
    return '#D37133';
  }
  if (name.match(/sql/i)) {
    return '#3E6389';
  }
  if (name.match(/datasync/i)) {
    return '#4BA5E2';
  }
  if (name.match(/export/i)) {
    return '#c061e8';
  }
  if (name.match(/import/i)) {
    return '#744FC6';
  }

  return '#5C95FF';
}

export function adjustColor(color: string, percentage: number): string {
  return `#${  color.replace(/^#/, '').replace(/../g, color => (`0${Math.min(255, Math.max(0, parseInt(color, 16) + percentage)).toString(16)}`).substr(-2))}`;
}

export function hexToRgbA(hex: string, alpha: number = 1){
  let c;
  if (/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)) {
    c = hex.substring(1).split('');
    if(c.length === 3){
      c= [c[0], c[0], c[1], c[1], c[2], c[2]];
    }
    c= `0x${c.join('')}`;
    // @ts-ignore
    // eslint-disable-next-line no-bitwise
    return `rgba(${[(c>>16)&255, (c>>8)&255, c&255].join(',')},${alpha})`;
  }
  throw new Error('Bad Hex');
}

