const fs = require('fs');
const path = require('path');
function walk(dir) {
  let results = [];
  const list = fs.readdirSync(dir);
  list.forEach(file => {
    file = path.join(dir, file);
    const stat = fs.statSync(file);
    if (stat && stat.isDirectory()) { 
      results = results.concat(walk(file));
    } else { 
      results.push(file);
    }
  });
  return results;
}
walk('app/src').filter(f => f.endsWith('.kt') || f.endsWith('.xml') || f.endsWith('.kts')).forEach(f => {
  let content = fs.readFileSync(f, 'utf8');
  if (content.includes('com.example')) {
    fs.writeFileSync(f, content.replace(/com\.example/g, 'com.aistudio.sublimationerp'));
  }
});
