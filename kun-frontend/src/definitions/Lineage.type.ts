import { Dataset } from '@/definitions/Dataset.type';

export interface LineageNode {
  /** Lineage node global identifier. REQUIRED and should always be unique. */
  id: string;
  /** internal data */
  data: Dataset & {
    /** Does it have more unexpanded upstream? */
    expandableUpstream?: boolean;
    /** Does it have more unexpanded downstream? */
    expandableDownstream?: boolean;
    /** Is it selected? */
    selected?: boolean;
  };
  /** width of the node rectangle. Defaults to 280px */
  width?: number;
  /** height of the node rectangle. Defaults to 108px */
  height?: number;
}

export interface LineageEdge {
  /** id of upstream node */
  src: string;
  /** id of downstream node */
  dest: string;
  /** Is it selected? */
  selected?: boolean;
}

export interface LineageNodeGroupElementState {

}
