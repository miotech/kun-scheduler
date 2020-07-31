import SafeUrlAssembler from 'safe-url-assembler';
import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';

export default function assembleRemotePdfFileUrl(originUrl: string = '') {
  if (!originUrl) {
    return '';
  }
  return SafeUrlAssembler()
    .template('/pdf/file/remote')
    .query({
      url: originUrl,
    })
    .prefix(DEFAULT_API_PREFIX)
    .toString();
}
