const fabric = require('@umijs/fabric');


module.exports = {
  ...fabric.prettier,
  "singleQuote": true,
  "trailingComma": "all",
  "semi": true,
  "bracketSpacing": true,
  "arrowParens": "avoid",
  "printWidth": 80,
}
