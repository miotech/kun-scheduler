import React from 'react';
import { Card as AntCard, Row, Col, Divider } from 'antd';
import { cloneDeep } from 'lodash';
import omitBy from 'lodash/omitBy';
import { CardProps } from 'antd/es';

import { CardGrid } from './CardGrid';

import './index.less';

interface Props extends CardProps {
  split?: true;
  gutter?: number;
  wrap?: boolean;
}

const getColProps = (node: React.ReactElement) => {
  return omitBy(cloneDeep(node?.props || {}), ['chilren', 'className']);
};

const renderChildren = (childrens: React.ReactElement[], split?: React.ReactNode, gutter?: number, wrap?: boolean) => {
  if (split) {
    const marginSize = gutter ? Math.floor(gutter / 2) : 0;
    const sumOfSpans =
      childrens.reduce((res, cur) => {
        return res + (cur?.props?.span ?? 0);
      }, 0) && 24;
    return childrens.map((node, index) => {
      const colProps = getColProps(node);
      if (!wrap) {
        const flex = `calc(${(colProps?.span ?? 1) / sumOfSpans}% - ${marginSize * 2 * (childrens.length - 1)}px)`;
        delete colProps.span;
        colProps.flex = flex;
      }
      if (index < childrens.length - 1) {
        return [
          <Col {...colProps}>{node}</Col>,
          <Col>
            <Divider type="vertical" style={{ height: '100%', margin: `0 ${marginSize}px` }} />
          </Col>,
        ];
      }
      return <Col {...colProps}>{node}</Col>;
    });
  }
  return childrens.map(node => {
    const colProps = getColProps(node);
    return <Col {...colProps}>{node}</Col>;
  });
};

const Card = ({ title, children, split, gutter, wrap, ...resetProps }: Props) => {
  const child = Array.isArray(children) ? children : [children];
  return (
    <AntCard title={title} bordered={false} className="miotech-card" {...resetProps}>
      <Row gutter={split ? 0 : gutter ?? 8}>{renderChildren(child, split, gutter, wrap)}</Row>
    </AntCard>
  );
};

Card.CardGrid = CardGrid;

export { Card };
