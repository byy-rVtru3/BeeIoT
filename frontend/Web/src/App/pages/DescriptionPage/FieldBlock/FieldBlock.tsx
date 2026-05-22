import {
  Box as DescBox,
  TextField as DescTextField,
  Typography as DescTypography,
} from '@mui/material';
import { forwardRef, type ElementType, type InputHTMLAttributes } from 'react';
import { IMaskInput, type IMaskInputProps } from 'react-imask';

type FieldBlockProps = {
  label: string;
  hint?: string;
  value: string;
  onChange: (value: string) => void;
  multiline?: boolean;
  rows?: number;
  counter?: [number, number];
  maxLength?: number;
  placeholder?: string;
  errorText?: string;
  mask?: IMaskInputProps['mask'];
  unmask?: IMaskInputProps['unmask'];
  inputMode?: InputHTMLAttributes<HTMLInputElement>['inputMode'];
};

type MaskedInputProps = IMaskInputProps<HTMLInputElement> & {
  onChange: (event: { target: { value: string } }) => void;
};

// Adapter for MUI TextField inputComponent.
const MaskedInput = forwardRef<HTMLInputElement, MaskedInputProps>(function MaskedInput(
  { onChange, ...rest },
  ref
) {
  return (
    <IMaskInput
      {...rest}
      inputRef={ref}
      overwrite
      onAccept={(value) => onChange({ target: { value: String(value) } })}
    />
  );
});

const FieldBlock = ({
  label,
  hint,
  value,
  onChange,
  multiline,
  rows,
  counter,
  maxLength,
  placeholder,
  errorText,
  mask,
  unmask,
  inputMode,
}: FieldBlockProps) => (
  <DescBox>
    <DescBox
      sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', mb: 0.75 }}
    >
      <DescTypography sx={{ fontSize: 14, fontWeight: 600 }}>{label}</DescTypography>
      {counter ? (
        <DescTypography
          sx={{
            fontSize: 12,
            color: counter[0] > counter[1] * 0.9 ? 'rgb(192,0,0)' : 'rgba(0,0,0,0.45)',
          }}
        >
          {counter[0]} / {counter[1]}
        </DescTypography>
      ) : null}
    </DescBox>
    <DescTextField
      fullWidth
      multiline={multiline}
      rows={rows}
      value={value}
      onChange={(event) => onChange(event.target.value.slice(0, maxLength ?? Infinity))}
      placeholder={placeholder}
      error={Boolean(errorText)}
      slotProps={{
        input: {
          ...(mask
            ? {
                inputComponent: MaskedInput as ElementType,
                inputProps: {
                  mask,
                  unmask,
                  inputMode: inputMode ?? 'numeric',
                },
              }
            : {
                inputProps: {
                  inputMode,
                },
              }),
        },
      }}
    />
    {errorText ? (
      <DescTypography sx={{ fontSize: 12, color: 'rgb(192,0,0)', mt: 0.75 }}>
        {errorText}
      </DescTypography>
    ) : hint ? (
      <DescTypography sx={{ fontSize: 12, color: 'rgba(0,0,0,0.5)', mt: 0.75 }}>
        {hint}
      </DescTypography>
    ) : null}
  </DescBox>
);

export type { FieldBlockProps };

export default FieldBlock;
