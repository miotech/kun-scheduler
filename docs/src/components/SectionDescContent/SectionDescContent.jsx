import React from 'react'
import c from 'clsx'
import css from './SectionDescContent.module.css'

export default function SectionDescContent({className, title, subTitle}) {
  return (
    <div className={c(css.SectionDescContent, className)}>
      <div className={css.title}>
        {title}
      </div>
      <div className={css.colorBlock} />
      <div className={css.subTitle}>
        {subTitle}
      </div>
    </div>
  )
}
