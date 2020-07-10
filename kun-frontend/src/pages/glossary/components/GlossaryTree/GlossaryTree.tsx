/* eslint-disable no-underscore-dangle, no-inner-declarations, no-param-reassign */
// @ts-nocheck
import { useHistory } from 'umi';
import React, { memo, useEffect, useRef, useCallback } from 'react';
import * as d3 from 'd3';
import { PlusOutlined, MinusOutlined } from '@ant-design/icons';

import { GlossaryNode } from '@/rematch/models/glossary';
import useRedux from '@/hooks/useRedux';

import glossarySvg from './glossary.svg';
import minus from './minus.svg';
import plus from './plus.svg';

import styles from './GlossaryTree.less';

export interface Props {
  rootNode: GlossaryNode | null;
}

export default memo(function GlossaryTree({ rootNode }: Props) {
  const history = useHistory();

  const svgContentRef = useRef(null);
  const svgRef = useRef(null);

  const thisZoom = useRef(null);

  const { dispatch } = useRedux(() => {});

  useEffect(() => {
    if (rootNode) {
      const marginWidth = 35;
      const marginHeight = 24;

      const nodeWidth = 166;
      const nodeHeight = 40;

      const svg = d3
        .select('#tree')
        .append('svg')
        .attr('width', '100%')
        .attr('height', '100%')
        .attr('font-size', 14)
        .attr('preserveAspectRatio', 'xMinYMin slice')
        .style('color', '#d8d8d8');

      svgRef.current = svg;

      const svgContent = svg.append('g').attr('class', 'svgContent');
      svgContentRef.current = svgContent;

      const zoom = d3.zoom().on('zoom', function zoomFunc() {
        svgContent.attr('transform', d3.event.transform);
      });

      thisZoom.current = zoom;

      svg.call(zoom).on('wheel.zoom', null);

      const duration = 750;

      const data = rootNode;

      data.depth = 0;
      data.verticalIndex = 0;

      // clip-path
      svg
        .append('clipPath')
        .attr('id', 'text-clip')
        .append('rect')
        .attr('x', 40)
        .attr('y', 0)
        .attr('rx', 3)
        .attr('ry', 3)
        .attr('width', 90)
        .attr('height', 40);

      update(data);

      function update(sourceNode) {
        let i = 0;
        let lastDepth = 0;
        // 储存每个层级循环到当前的 verticalIndex, 用于计算垂直高度
        const verticalIndexPerDepth = {};

        // 转换成hierarchy结构, 才能用在layout中
        const root = d3.hierarchy(data).eachBefore(d => {
          if (d.depth === 0) {
            lastDepth = d.depth;
            d.verticalIndex = i;
          } else if (d.depth > lastDepth) {
            lastDepth = d.depth;
            d.verticalIndex = i;
          } else {
            lastDepth = d.depth;
            i += 1;
            d.verticalIndex = i;
          }
          d.data.verticalIndex = i;

          d.brotherVerticalIndex = verticalIndexPerDepth[d.depth]
            ? verticalIndexPerDepth[d.depth]
            : 0;
          verticalIndexPerDepth[d.depth] = i;
        });

        const node = svgContent
          .selectAll('g.node')
          .data(root.descendants(), d => {
            return d.data.id;
          });

        const getTransform = (depth, verticalIndex) => {
          const trX = (depth + 1) * marginWidth + depth * nodeWidth;
          const trY =
            (verticalIndex + 1) * marginHeight + verticalIndex * nodeHeight;
          return `translate(${trX}, ${trY})`;
        };

        const verticalIndex =
          sourceNode?.data?.verticalIndex ?? sourceNode.verticalIndex;

        const nodeEnter = node
          .enter()
          .append('g')
          .attr('title', d => d.data.name)
          .attr('class', 'node')
          .attr('transform', () => {
            return getTransform(sourceNode.depth, verticalIndex);
          });

        nodeEnter
          .append('rect')
          .attr('fill', 'rgba(224, 224, 224, 0.2)')
          .attr('stroke', 'rgba(224, 224, 224, 0.8)')
          .attr('x', 0)
          .attr('y', 0)
          .attr('rx', 3)
          .attr('ry', 3)
          .attr('width', 166)
          .attr('height', 40)
          .each(function textfunc(d) {
            if (d.data.id !== 'root') {
              const canNode = d3.select(this);

              canNode.attr('class', styles.nodeText).on('click', n => {
                history.push(`/data-discovery/glossary/${n.data.id}`);
              });
            }
          });

        nodeEnter
          .append('image')
          .attr('xlink:href', glossarySvg)
          .attr('class', styles.glossaryIcon);

        const textnode = nodeEnter
          .append('g')
          .attr('clip-path', 'url(#text-clip)')
          .append('text')
          .attr('class', styles.textnode)
          .text(d => d.data.name)
          .attr('x', 42)
          .attr('y', 25)
          .attr('font-size', 14)
          .attr('fill', '#526079');

        textnode.each(function textfunc(d) {
          if (d.data.id !== 'root') {
            const canNode = d3.select(this);

            canNode.attr('class', styles.nodeText).on('click', n => {
              history.push(`/data-discovery/glossary/${n.data.id}`);
            });
          }
        });

        nodeEnter.append('title').text(d => d.data.name);

        nodeEnter.each(addButton);

        svgContent
          .selectAll('g.node')
          .transition()
          .duration(duration)
          .attr('transform', d => {
            const trX = (d.depth + 1) * marginWidth + d.depth * nodeWidth;
            const trY =
              (d.verticalIndex + 1) * marginHeight +
              d.verticalIndex * nodeHeight;
            return `translate(${trX}, ${trY})`;
          });

        node
          .exit()
          .transition()
          .duration(duration)
          .attr('transform', () => {
            const trX =
              (sourceNode.depth + 1) * marginWidth +
              sourceNode.depth * nodeWidth;
            const trY =
              (verticalIndex + 1) * marginHeight + verticalIndex * nodeHeight;
            return `translate(${trX}, ${trY})`;
          })
          .remove();

        const link = svgContent
          .selectAll('path.linkPath')
          .data(root.links(), d => {
            return d.target.data.id;
          });

        link
          .enter()
          .insert('path', 'g')
          .attr('fill', 'none')
          .attr('stroke', '#999')
          .attr('stroke-dasharray', '2, 2')
          .attr('d', () => {
            const M1 =
              (sourceNode.depth + 1) * nodeWidth +
              (sourceNode.depth + 1) * marginWidth;
            const M2 =
              (verticalIndex + 1 / 2) * nodeHeight +
              (verticalIndex + 1) * marginHeight;

            return `
            M${M1},${M2}
            h0
            v0
            h0
          `;
          })
          .attr('class', 'linkPath');

        svgContent
          .selectAll('path.linkPath')
          .transition()
          .duration(duration)
          .attr('d', d => {
            let M1;
            let M2;
            let h1;
            let v;
            if (d.source.verticalIndex === d.target.verticalIndex) {
              M1 =
                (d.source.depth + 1) * nodeWidth +
                (d.source.depth + 1) * marginWidth;
              M2 =
                (d.source.verticalIndex + 1 / 2) * nodeHeight +
                (d.source.verticalIndex + 1) * marginHeight;
              h1 = marginWidth / 2;
              v = 0;
            } else {
              M1 =
                (d.source.depth + 1) * nodeWidth +
                (d.source.depth + 1 + 1 / 2) * marginWidth;
              M2 =
                (d.target.brotherVerticalIndex + 1 / 2) * nodeHeight +
                (d.target.brotherVerticalIndex + 1) * marginHeight;
              h1 = 0;
              v =
                (d.target.verticalIndex - d.target.brotherVerticalIndex) *
                (nodeHeight + marginHeight);
            }

            return `
              M${M1},${M2}
              h${h1}
              v${v}
              h${marginWidth / 2}
            `;
          });

        link
          .exit()
          .transition()
          .duration(duration)
          .attr('d', () => {
            const M1 =
              (sourceNode.depth + 1) * nodeWidth +
              (sourceNode.depth + 1) * marginWidth;
            const M2 =
              (verticalIndex + 1 / 2) * nodeHeight +
              (verticalIndex + 1) * marginHeight;

            return `
            M${M1},${M2}
            h0
            v0
            h0
          `;
          })
          .remove();
      }

      function addPlusMinusIcon(node, operate) {
        const buttonG = node
          .append('g')
          .attr('class', styles.buttonG)
          .attr('x', 134)
          .attr('y', 10)
          .on('click', click);

        buttonG
          .append('rect')
          .attr('stroke', '#e0e0e0')
          .attr('fill', 'white')
          .attr('rx', 3)
          .attr('ry', 3)
          .attr('width', 20)
          .attr('height', 20);

        if (operate === 'plus') {
          buttonG
            .append('image')
            .attr('xlink:href', minus)
            .attr('class', styles.minusPlusIcon);
        } else {
          buttonG
            .append('image')
            .attr('xlink:href', plus)
            .attr('class', styles.minusPlusIcon);
        }
      }

      function addButton(d) {
        if (d.data.childrenCount && d.data.id !== 'root') {
          const canNode = d3.select(this);

          if (d.data.children) {
            addPlusMinusIcon(canNode, 'plus');
          } else {
            addPlusMinusIcon(canNode, 'minus');
          }
        }
      }

      function click(d) {
        const thisNode = d3.select(this);
        thisNode.selectAll(`rect`).remove();

        thisNode
          .append('rect')
          .attr('stroke', '#e0e0e0')
          .attr('fill', 'white')
          .attr('rx', 3)
          .attr('ry', 3)
          .attr('width', 20)
          .attr('height', 20);

        if (!d.data.childrenCount) {
          return;
        }
        if (d.data.children) {
          d.data.children = null;
          thisNode
            .append('image')
            .attr('xlink:href', plus)
            .attr('class', styles.minusPlusIcon);
          update(d);
        } else {
          thisNode
            .append('image')
            .attr('xlink:href', minus)
            .attr('class', styles.minusPlusIcon);
          dispatch.glossary
            .fetchNodeChildAndUpdateNode({ nodeData: d.data })
            .then(() => {
              update(d);
            });
        }
      }
    }

    return () => {
      const treeElement = document.getElementById('tree');
      if (treeElement) {
        treeElement.innerHTML = '';
      }
    };
  }, [dispatch.glossary, history, rootNode]);

  const handleClickAdd = useCallback(() => {
    svgRef.current
      .transition()
      .duration(350)
      .call(thisZoom.current.scaleBy, 1.2);
  }, []);

  const handleClickSub = useCallback(() => {
    svgRef.current
      .transition()
      .duration(350)
      .call(thisZoom.current.scaleBy, 0.8);
  }, []);

  return (
    <div className={styles.glossaryTree}>
      <div className={styles.scaleButtonRow}>
        <div className={styles.scaleButton} onClick={handleClickAdd}>
          <PlusOutlined className={styles.scaleButtonIcon} />
        </div>
        <div className={styles.scaleButton} onClick={handleClickSub}>
          <MinusOutlined className={styles.scaleButtonIcon} />
        </div>
      </div>

      <div id="tree" className={styles.treeContainer} />
    </div>
  );
});
