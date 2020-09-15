import React from 'react';
import c from 'clsx';
import css from './Card.less';

interface Props {
  children: React.ReactNode;
  className?: string;
}

export default function Card({ children, className = '' }: Props) {
  return <div className={c(css.card, className)}>{children}</div>;
}
