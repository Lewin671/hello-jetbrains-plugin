import { useEffect, useRef } from 'react';

export const useAutoScroll = (dependencies: any[]) => {
  const scrollRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    scrollRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, dependencies);

  return scrollRef;
}; 