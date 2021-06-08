import { tokenize } from '@/components/CodeEditor/language/sparksql-tokenizer';

// eslint-disable-next-line no-restricted-globals
const ctx: Worker = self as any;

ctx.addEventListener('message', (event) => {
  const sql = event.data.sql as string;
  try {
    const tokens = tokenize(sql);
    ctx.postMessage({
      tokens: tokens.filter(t => (t.text != null) && (t.text.trim().length > 0)).map(t => t.text),
      error: null,
    });
  } catch (e) {
    ctx.postMessage({
      tokens: null,
      error: e,
    });
  }
});

