import React from 'react';
import { PDFDocumentProxy, PDFPageProxy } from 'pdfjs-dist';

export type RenderFunction = () => JSX.Element;

export interface DocumentProps {
  className?: string | string[];
  error?: string | React.ReactElement | RenderFunction;
  externalLinkTarget?: '_self' | '_blank' | '_parent' | '_top';
  file: any;
  inputRef?: React.LegacyRef<HTMLDivElement>;
  loading?: string | React.ReactElement | RenderFunction;
  noData?: string | React.ReactElement | RenderFunction;
  onItemClick?: ({ pageNumber }: { pageNumber: string }) => void;
  onLoadError?: (error: Error) => void;
  onLoadSuccess?: (pdf: PDFDocumentProxy) => void;
  onPassword?: (callback: (...args: any[]) => any) => void;
  onSourceError?: (error: Error) => void;
  onSourceSuccess?: () => void;
  options?: any;
  renderMode?: 'canvas' | 'svg' | 'none';
  rotate?: number;
  children?: React.ReactNode;
}

export interface PDFPageItem {
  _transport: object;
  commonObjs: object;
  getAnnotations: (...args: any[]) => any;
  getTextContent: (...args: any[]) => any;
  getViewport: (...args: any[]) => any;
  render: (...args: any[]) => any;
}

export interface TextLayerItemInternal {
  fontName: string;
  itemIndex: number;
  page: PDFPageItem;
  rotate?: 0 | 90 | 180 | 270;
  scale?: number;
  str: string;
  transform: number[];
  width: number;
}

export interface LoadingProcessData {
  loaded: number;
  total: number;
}

export interface TextItem {
  str: string;
  dir: string;
  transform: number[];
  width: number;
  height: number;
  fontName: string;
}

export interface PageProps {
  className?: string | string[];
  customTextRenderer?: (layer: TextLayerItemInternal) => JSX.Element;
  error?: string | React.ReactElement | RenderFunction;
  height?: number;
  inputRef?: React.LegacyRef<HTMLDivElement>;
  loading?: string | React.ReactElement | RenderFunction;
  noData?: string | React.ReactElement | RenderFunction;
  onLoadError?: (error: Error) => void;
  onLoadProgress?: (data: LoadingProcessData) => void;
  onLoadSuccess?: (page: PDFPageProxy) => void;
  onRenderError?: (error: Error) => void;
  onRenderSuccess?: () => void;
  onGetAnnotationsSuccess?: (annotations: any) => void;
  onGetAnnotationsError?: (error: Error) => void;
  onGetTextSuccess?: (items: TextItem[]) => void;
  onGetTextError?: (error: Error) => void;
  pageIndex?: number;
  pageNumber?: number;
  renderAnnotationLayer?: boolean;
  renderInteractiveForms?: boolean;
  renderMode?: 'canvas' | 'svg' | 'none';
  renderTextLayer?: boolean;
  rotate?: number;
  scale?: number;
  width?: number;
}

export const Document: React.ReactType<DocumentProps>;

export const Page: React.ReactType<PageProps>;
