import { Request, Response } from 'express';
import { wrapResponseData } from '../../mock-commons/utils/wrap-response';

export function getWhoAmI(req: Request, res: Response) {
  return res.json({
    username: 'mock',
  });
}

export function mockFetchUsersList(req: Request, res: Response) {
  return res.json(wrapResponseData([
    {
      "permissions": [
        "DATA_DISCOVERY",
        "DATA_DEVELOPMENT",
        "PDF_GENERAL"
      ],
      "id": "80642125028392960",
      "username": "joshoy"
    },
    {
      "permissions": [
        "DATA_DISCOVERY",
        "DATA_DEVELOPMENT",
        "PDF_GENERAL"
      ],
      "id": "81199142260441088",
      "username": "jentle"
    }
  ]));
}

export default {};
