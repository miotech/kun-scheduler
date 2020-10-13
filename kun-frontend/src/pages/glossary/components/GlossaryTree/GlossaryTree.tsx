/* eslint-disable no-underscore-dangle, no-inner-declarations, no-param-reassign */
// @ts-nocheck
import { useHistory } from 'umi';
import React, { memo, useEffect, useRef, useCallback } from 'react';
import * as d3 from 'd3';
import { PlusOutlined, MinusOutlined } from '@ant-design/icons';

import { GlossaryNode } from '@/rematch/models/glossary';
import useRedux from '@/hooks/useRedux';
import moveArrayItem from '@/utils/moveArrayItem';
import { updateGlossaryOrderService } from '@/services/glossary';

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

  const updateGlossaryOrderApi = useCallback(async (id, prevId) => {
    await updateGlossaryOrderService(id, prevId);
  }, []);

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

      function update(sourceNode, needDuration = true) {
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

        // 根据横纵层级深得到偏移
        const getTransform = (depth, verticalIndex) => {
          const trX = (depth + 1) * marginWidth + depth * nodeWidth;
          const trY =
            (verticalIndex + 1) * marginHeight + verticalIndex * nodeHeight;

          return `translate(${trX}, ${trY})`;
        };

        // 拿到源node的垂直层级
        const verticalIndex =
          sourceNode?.data?.verticalIndex ?? sourceNode.verticalIndex;

        // 新添加的node
        const nodeEnter = node
          .enter()
          .append('g')
          .attr('title', d => d.data.name)
          .attr('class', 'node')
          .attr('transform', () => {
            // 初始位置是展开源node位置
            return getTransform(sourceNode.depth, verticalIndex);
          });

        // 拖拽方法
        const drag = d3
          .drag()
          .on('start', function dragFunc(d) {
            if (d.data.id === 'root' || !d.parent) {
              return;
            }
            d.data.oldX = d.data.trX;
            d.data.oldY = d.data.trY;
          })
          .on('drag', function dragFunc(d) {
            if (d.data.id === 'root' || !d.parent) {
              return;
            }
            const toX = d.data.trX;
            const toY = d3.event.dy + d.data.trY;
            d.data.trX = toX;
            d.data.trY = toY;
            d3.select(this).attr('transform', `translate(${toX}, ${toY})`);
          })
          .on('end', function dragFunc(d) {
            if (d.data.id === 'root' || !d.parent) {
              return;
            }
            const brothers = d.parent.data.children;
            if (brothers && brothers.length > 1) {
              // 以前的 位置
              const oldIndex = brothers.findIndex(
                item => item.id === d.data.id,
              );
              // 应该移动到的 位置
              let shouldIndex;
              for (
                let brotherIndex = 0;
                brotherIndex < brothers.length;
                brotherIndex += 1
              ) {
                const brother = brothers[brotherIndex];

                // 如果是往下面拖拽
                if (d.data.trY > d.data.oldY) {
                  // 如果不是拖拽到最后一个
                  if (brotherIndex !== brothers.length - 1) {
                    // 判断是否拖拽到对应的区间了
                    if (
                      d.data.id !== brother.id &&
                      d.data.trY > brother.trY &&
                      d.data.trY < brothers[brotherIndex + 1].trY
                    ) {
                      shouldIndex = brotherIndex;
                    }
                    // 如果拖拽到最后一个, 并且自己本身并不是最后一个
                  } else if (
                    d.data.id !== brother.id &&
                    d.data.trY > brother.trY
                  ) {
                    shouldIndex = brotherIndex;
                  }
                  // 如果往上面拖拽, 并且不是拖拽到第一个
                } else if (brotherIndex !== 0) {
                  // 判断是否拖拽到对应的区间了
                  if (
                    d.data.id !== brother.id &&
                    d.data.trY < brother.trY &&
                    d.data.trY > brothers[brotherIndex - 1].trY
                  ) {
                    shouldIndex = brotherIndex;
                  }
                  // 如果是拖拽到了第一个, 并且自己本身并不是第一个
                } else if (
                  d.data.id !== brother.id &&
                  d.data.trY < brother.trY
                ) {
                  shouldIndex = brotherIndex;
                }
              }
              // 如果挪动了位置
              if (shouldIndex || shouldIndex === 0) {
                const newBrothers = moveArrayItem(
                  brothers,
                  oldIndex,
                  shouldIndex,
                );
                // 得到新排列后的children
                d.parent.data.children = newBrothers;

                // 访问api
                const prevChild = d.parent.data.children?.[shouldIndex - 1];
                updateGlossaryOrderApi(d.data.id, prevChild?.id);
              }

              update(d.parent);
            } else {
              d.data.trX = d.data.oldX;
              d.data.trY = d.data.oldY;
              d3.select(this)
                .transition()
                .duration(100)
                .attr('transform', `translate(${d.data.oldX}, ${d.data.oldY})`);
            }
          });

        nodeEnter.call(drag);

        // 添加glossary外层方块
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

              // 添加点击事件
              canNode.attr('class', styles.nodeText).on('click', n => {
                history.push(`/data-discovery/glossary/${n.data.id}`);
              });
            }
          });

        // 添加glossary图标
        nodeEnter
          .append('image')
          .attr('xlink:href', glossarySvg)
          .attr('class', styles.glossaryIcon);

        // 添加文字 (过长需要截断)
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

        // 添加跳转链接
        textnode.each(function textfunc(d) {
          if (d.data.id !== 'root') {
            const canNode = d3.select(this);

            canNode.attr('class', styles.nodeText).on('click', n => {
              history.push(`/data-discovery/glossary/${n.data.id}`);
            });
          }
        });

        // 如果截断了, 需要在title中能hover出来
        nodeEnter.append('title').text(d => d.data.name);

        // 添加加减按钮
        nodeEnter.each(addButton);

        // 动画挪动到自己应该在的位置
        svgContent
          .selectAll('g.node')
          .transition()
          .duration(needDuration ? duration : 0)
          .attr('transform', d => {
            const trX = (d.depth + 1) * marginWidth + d.depth * nodeWidth;
            const trY =
              (d.verticalIndex + 1) * marginHeight +
              d.verticalIndex * nodeHeight;
            d.data.trX = trX;
            d.data.trY = trY;
            return `translate(${trX}, ${trY})`;
          });

        // 折叠了的, 先回到源节点的位置, 然后再删除掉
        node
          .exit()
          .transition()
          .duration(needDuration ? duration : 0)
          .attr('transform', () => {
            const trX =
              (sourceNode.depth + 1) * marginWidth +
              sourceNode.depth * nodeWidth;
            const trY =
              (verticalIndex + 1) * marginHeight + verticalIndex * nodeHeight;
            return `translate(${trX}, ${trY})`;
          })
          .remove();

        // 处理连线
        const link = svgContent
          .selectAll('path.linkPath')
          .data(root.links(), d => {
            return d.target.data.id;
          });

        // 首先, 新加的连线需要在源节点位置
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

        // 动画从源节点挪动到应该在的位置
        svgContent
          .selectAll('path.linkPath')
          .transition()
          .duration(needDuration ? duration : 0)
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

        // 删除的连线
        link
          .exit()
          .transition()
          .duration(needDuration ? duration : 0)
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

      // 添加加减号按钮
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

      // 点击加减号事件
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
  }, [dispatch.glossary, history, rootNode, updateGlossaryOrderApi]);

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
