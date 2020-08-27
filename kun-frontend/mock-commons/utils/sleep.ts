export default function sleep(ms: number): Promise<void> {
  return new Promise<void>(() => {
    setTimeout(() => Promise.resolve(), ms);
  });
}
