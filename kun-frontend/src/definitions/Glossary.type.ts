export enum Operation {
  ADD_GLOSSARY = 'ADD_GLOSSARY',
  EDIT_GLOSSARY = 'EDIT_GLOSSARY',
  READ_GLOSSARY = 'READ_GLOSSARY',
  SEARCH_GLOSSARY = 'SEARCH_GLOSSARY',
  REMOVE_GLOSSARY = 'REMOVE_GLOSSARY',
  EDIT_GLOSSARY_CHILD = 'EDIT_GLOSSARY_CHILD',
  COPY_GLOSSARY = 'COPY_GLOSSARY',
  PASTE_GLOSSARY = 'PASTE_GLOSSARY',
  EDIT_GLOSSARY_RESOURCE = 'EDIT_GLOSSARY_RESOURCE',
  EDIT_GLOSSARY_EDITOR = 'EDIT_GLOSSARY_EDITOR',
}

export interface GlossaryRoleResponse {
  kunRole: string;
  operations: Operation;
  securityModule: string;
  sourceSystemId: string;
}

export interface GlossaryEditorParams {
  userName: string;
  id: string;
}
