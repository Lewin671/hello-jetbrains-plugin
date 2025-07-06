import { useEffect, useRef } from 'react';

export const useAutoScroll = (dependencies: any[]) => {
  const scrollRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    scrollRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    scrollToBottom();
  }, dependencies);

  return scrollRef;
}; 