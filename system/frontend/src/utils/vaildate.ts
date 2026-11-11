


export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

 export function validUsername(str) {
  const valid_map = ['admin', 'editor','xwya']
  return valid_map.indexOf(str.trim()) >= 0
}
